package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.SendMessageRequest;
import com.sep490.vtuber_fanhub.dto.responses.MessageResponse;
import com.sep490.vtuber_fanhub.dto.responses.MetadataResponse;
import com.sep490.vtuber_fanhub.dto.responses.PaginatedResponse;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.models.*;
import com.sep490.vtuber_fanhub.models.Enum.MetadataType;
import com.sep490.vtuber_fanhub.repositories.ChatMessageRepository;
import com.sep490.vtuber_fanhub.repositories.ChatSessionRepository;
import com.sep490.vtuber_fanhub.repositories.PostMediaRepository;
import com.sep490.vtuber_fanhub.repositories.PostRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PostMediaRepository postMediaRepository;
    private final AiResponseService aiResponseService;

    @Override
    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request, String username) {
        try{
            Optional<User> tokenUser = userRepository.findByUsernameAndIsActive(username);
            if (tokenUser.isEmpty()) {
                throw new CustomAuthenticationException("Authentication failed");
            }

            String message = request.getContent();

            User user = tokenUser.get();

            ChatSession chatSession;

            Optional<ChatSession> session = chatSessionRepository.findByUser_Id(user.getId());
            if(session.isEmpty()) {
                ChatSession newSession = new ChatSession();
                newSession.setUser(user);
                newSession = chatSessionRepository.save(newSession);
                chatSession = newSession;
            }
            else chatSession = session.get();

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSenderRole("USER");
            chatMessage.setCreatedAt(Instant.now());
            chatMessage.setContent(message);
            chatMessage.setSession(chatSession);
            chatMessageRepository.save(chatMessage);

            return aiResponseService.generateAndSendReply(user, message);
        }
        catch(Exception e){
            throw new RuntimeException("Error while sending message");
        }
    }

    @Override
    public PaginatedResponse<MessageResponse> getMessagesPaginated(String username, int page, int size) {
        try {
            // 1. Basic Validation
            Optional<User> tokenUser = userRepository.findByUsernameAndIsActive(username);
            if (tokenUser.isEmpty()) {
                throw new CustomAuthenticationException("Authentication failed");
            }

            Optional<ChatSession> chatSession = chatSessionRepository.findByUser_Id(tokenUser.get().getId());
            if (chatSession.isEmpty()) {
                return PaginatedResponse.<MessageResponse>builder()
                        .data(new ArrayList<>())
                        .currentPage(page)
                        .pageSize(size)
                        .totalElements(0)
                        .totalPages(0)
                        .hasNext(false)
                        .hasPrevious(false)
                        .build();
            }

            PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<ChatMessage> messagesPage = chatMessageRepository.findAllWithMetadata(chatSession.get().getId(), pageable);
            List<ChatMessage> messages = messagesPage.getContent();

            // 3. Collect Unique Post IDs from metadata (Loop #1)
            Set<Long> postIds = new HashSet<>();
            for (ChatMessage msg : messages) {
                if (msg.getHasMetadata() != null && msg.getHasMetadata()) {
                    for (ChatMessageMetadata meta : msg.getMetadataList()) {
                        if (meta.getMetadataType() == MetadataType.POST) {
                            postIds.add(meta.getTargetId());
                        }
                    }
                }
            }

            // 4. Batch Fetch Posts and Images into Maps (Loop #2)
            Map<Long, Post> postMap = new HashMap<>();
            for (Post p : postRepository.findAllById(postIds)) {
                postMap.put(p.getId(), p);
            }

            Map<Long, String> postImageMap = new HashMap<>();
            if (!postIds.isEmpty()) {
                for (PostMedia pm : postMediaRepository.findByPostIdIn(new ArrayList<>(postIds))) {
                    // Only store the first image URL we find for each post
                    if (!postImageMap.containsKey(pm.getPost().getId())) {
                        postImageMap.put(pm.getPost().getId(), pm.getMediaUrl());
                    }
                }
            }

            // 5. Build final responses (Loop #3)
            List<MessageResponse> messageResponses = new ArrayList<>();
            for (ChatMessage chatMessage : messages) {
                MessageResponse.MessageResponseBuilder builder = MessageResponse.builder()
                        .id(chatMessage.getId())
                        .createdAt(chatMessage.getCreatedAt())
                        .content(chatMessage.getContent())
                        .senderRole(chatMessage.getSenderRole())
                        .thought(chatMessage.getThought());

                // Link the metadata (Post info) if it exists
                if (chatMessage.getHasMetadata() != null && chatMessage.getHasMetadata()) {
                    for (ChatMessageMetadata meta : chatMessage.getMetadataList()) {
                        if (meta.getMetadataType() == MetadataType.POST) {
                            Post post = postMap.get(meta.getTargetId());
                            if (post != null) {
                                builder.metadataResponse(MetadataResponse.builder()
                                        .metadataType(MetadataType.POST)
                                        .postId(post.getId())
                                        .postTitle(post.getTitle())
                                        .postContent(post.getContent())
                                        .imagePreviewUrl(postImageMap.get(post.getId()))
                                        .build());
                                break; // Stop after finding the first post
                            }
                        }
                    }
                }
                messageResponses.add(builder.build());
            }

            return PaginatedResponse.<MessageResponse>builder()
                    .data(messageResponses)
                    .currentPage(page)
                    .pageSize(size)
                    .totalElements(messagesPage.getTotalElements())
                    .totalPages(messagesPage.getTotalPages())
                    .hasNext(messagesPage.hasNext())
                    .hasPrevious(messagesPage.hasPrevious())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while getting paginated messages: " + e.getMessage());
        }
    }

}
