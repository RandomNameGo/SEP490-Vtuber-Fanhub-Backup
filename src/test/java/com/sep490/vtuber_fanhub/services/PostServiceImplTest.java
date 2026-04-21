package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreatePollPostRequest;
import com.sep490.vtuber_fanhub.dto.requests.CreatePostRequest;
import com.sep490.vtuber_fanhub.dto.responses.PostResponse;
import com.sep490.vtuber_fanhub.exceptions.CooldownException;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.*;
import com.sep490.vtuber_fanhub.repositories.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private PostHashtagRepository postHashtagRepository;
    @Mock
    private PostMediaRepository postMediaRepository;
    @Mock
    private PostLikeRepository postLikeRepository;
    @Mock
    private FanHubRepository fanHubRepository;
    @Mock
    private FanHubMemberRepository fanHubMemberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private UserDailyMissionRepository userDailyMissionRepository;
    @Mock
    private VoteOptionRepository voteOptionRepository;
    @Mock
    private AuthService authService;
    @Mock
    private BanMemberService banMemberService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private PostVoteRepository postVoteRepository;
    @Mock
    private PostCommentRepository postCommentRepository;
    @Mock
    private PostValidationService postValidationServiceImplAsync;
    @Mock
    private JWTService jwtService;
    @Mock
    private GeminiAIServiceImpl geminiAIServiceImpl;
    @Mock
    private UserTrackService userTrackService;
    @Mock
    private UserDailyMissionService userDailyMissionService;
    @Mock
    private UserBookmarkRepository userBookmarkRepository;

    @InjectMocks
    private PostServiceImpl postService;

    private User currentUser;
    private FanHub fanHub;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testuser");
        currentUser.setRole("USER");

        fanHub = new FanHub();
        fanHub.setId(1L);
        User owner = new User();
        owner.setId(2L);
        fanHub.setOwnerUser(owner);
        fanHub.setIsActive(true);
        fanHub.setIsPrivate(false);
    }

    // --- createPost tests ---

    @Test
    void createPost_Success_TextPost() {
        CreatePostRequest request = new CreatePostRequest();
        request.setFanHubId(1L);
        request.setPostType("TEXT");
        request.setTitle("Title");
        request.setContent("Content");

        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));
        when(fanHubMemberRepository.findByHubIdAndUserId(1L, 1L)).thenReturn(Optional.of(new FanHubMember()));
        when(postRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        String result = postService.createPost(request, null, null);

        assertEquals("Created post successfully", result);
        verify(postRepository).save(any());
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void createPost_Failure_FanHubNotFound() {
        CreatePostRequest request = new CreatePostRequest();
        request.setFanHubId(1L);

        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.createPost(request, null, null));
    }

    @Test
    void createPost_Failure_Banned() {
        CreatePostRequest request = new CreatePostRequest();
        request.setFanHubId(1L);

        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));
        doThrow(new AccessDeniedException("Banned")).when(banMemberService).checkBanStatus(anyLong(), anyLong(), anyList());

        assertThrows(AccessDeniedException.class, () -> postService.createPost(request, null, null));
    }

    @Test
    void createPost_Failure_AccessDenied_NotMember() {
        CreatePostRequest request = new CreatePostRequest();
        request.setFanHubId(1L);
        request.setPostType("TEXT");

        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));
        when(fanHubMemberRepository.findByHubIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> postService.createPost(request, null, null));
    }

    @Test
    void createPost_Failure_InvalidPostType() {
        CreatePostRequest request = new CreatePostRequest();
        request.setFanHubId(1L);
        request.setPostType("INVALID");

        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));
        when(fanHubMemberRepository.findByHubIdAndUserId(1L, 1L)).thenReturn(Optional.of(new FanHubMember()));

        assertThrows(IllegalArgumentException.class, () -> postService.createPost(request, null, null));
    }

    @Test
    void createPost_Failure_ImagePost_NoMedia() {
        CreatePostRequest request = new CreatePostRequest();
        request.setFanHubId(1L);
        request.setPostType("IMAGE");

        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));
        when(fanHubMemberRepository.findByHubIdAndUserId(1L, 1L)).thenReturn(Optional.of(new FanHubMember()));

        assertThrows(IllegalArgumentException.class, () -> postService.createPost(request, null, null));
    }

    // --- createPollPost tests ---

    @Test
    void createPollPost_Success() {
        CreatePollPostRequest request = new CreatePollPostRequest();
        request.setFanHubId(1L);
        request.setTitle("Poll");
        request.setOptions(List.of("Opt1", "Opt2"));

        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));
        when(fanHubMemberRepository.findByHubIdAndUserId(1L, 1L)).thenReturn(Optional.of(new FanHubMember()));
        when(postRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        String result = postService.createPollPost(request);

        assertEquals("Created poll post successfully", result);
        verify(voteOptionRepository, times(2)).save(any());
    }

    @Test
    void createPollPost_Failure_FanHubNotFound() {
        CreatePollPostRequest request = new CreatePollPostRequest();
        request.setFanHubId(1L);

        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.createPollPost(request));
    }

    @Test
    void createPollPost_Failure_DuplicateOptions() {
        CreatePollPostRequest request = new CreatePollPostRequest();
        request.setFanHubId(1L);
        request.setOptions(List.of("Opt1", "Opt1"));

        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));
        when(fanHubMemberRepository.findByHubIdAndUserId(1L, 1L)).thenReturn(Optional.of(new FanHubMember()));

        assertThrows(IllegalArgumentException.class, () -> postService.createPollPost(request));
    }

    @Test
    void createPollPost_Failure_TooFewOptions() {
        CreatePollPostRequest request = new CreatePollPostRequest();
        request.setFanHubId(1L);
        request.setOptions(List.of("Opt1"));

        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));
        when(fanHubMemberRepository.findByHubIdAndUserId(1L, 1L)).thenReturn(Optional.of(new FanHubMember()));

        assertThrows(IllegalArgumentException.class, () -> postService.createPollPost(request));
    }

    // --- reviewPost tests ---

    @Test
    void reviewPost_Success_ApproveByOwner() {
        Post post = new Post();
        post.setId(1L);
        post.setHub(fanHub);
        post.setUser(currentUser);
        post.setStatus("PENDING");

        currentUser.setRole("VTUBER");
        fanHub.getOwnerUser().setId(1L); // Current user is owner

        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));

        String result = postService.reviewPost(1L, "APPROVED");

        assertEquals("Post approved successfully", result);
        assertEquals("APPROVED", post.getStatus());
        verify(postRepository).save(any());
    }

    @Test
    void reviewPost_Failure_NotFound() {
        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.reviewPost(1L, "APPROVED"));
    }

    @Test
    void reviewPost_Failure_InvalidStatus() {
        Post post = new Post();
        post.setId(1L);
        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThrows(IllegalArgumentException.class, () -> postService.reviewPost(1L, "INVALID"));
    }

    @Test
    void reviewPost_Failure_AccessDenied() {
        Post post = new Post();
        post.setId(1L);
        post.setHub(fanHub);

        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(fanHubRepository.findById(anyLong())).thenReturn(Optional.of(fanHub));
        when(fanHubMemberRepository.findByHubIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> postService.reviewPost(1L, "APPROVED"));
    }

    // --- pinPost tests ---

    @Test
    void pinPost_Success() {
        Post post = new Post();
        post.setId(1L);
        post.setHub(fanHub);
        post.setIsPinned(false);

        currentUser.setRole("VTUBER");
        fanHub.getOwnerUser().setId(1L);

        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));

        String result = postService.pinPost(1L);

        assertEquals("Post pinned successfully", result);
        assertTrue(post.getIsPinned());
    }

    @Test
    void pinPost_Failure_NotFound() {
        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.pinPost(1L));
    }

    @Test
    void pinPost_Failure_AccessDenied() {
        Post post = new Post();
        post.setId(1L);
        post.setHub(fanHub);

        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(fanHubRepository.findById(anyLong())).thenReturn(Optional.of(fanHub));
        when(fanHubMemberRepository.findByHubIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> postService.pinPost(1L));
    }

    // --- sendAiValidate tests ---

    @Test
    void sendAiValidate_Success() {
        Post post = new Post();
        post.setId(1L);
        post.setHub(fanHub);

        when(jwtService.getCurrentToken(any())).thenReturn("token");
        when(jwtService.getUsernameFromToken(any())).thenReturn("testuser");
        when(userRepository.findByUsernameAndIsActive(anyString())).thenReturn(Optional.of(currentUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(fanHubRepository.findById(anyLong())).thenReturn(Optional.of(fanHub));
        
        FanHubMember member = new FanHubMember();
        member.setRoleInHub("MODERATOR");
        when(fanHubMemberRepository.findByHub_IdAndUser_Id(anyLong(), anyLong())).thenReturn(Optional.of(member));
        when(postRepository.save(any())).thenReturn(post);

        String result = postService.sendAiValidate(1L);

        assertEquals("Job sent successfully!", result);
        verify(postValidationServiceImplAsync).validatePost(any());
    }

    @Test
    void sendAiValidate_Failure_Cooldown() {
        Post post = new Post();
        post.setId(1L);
        post.setHub(fanHub);
        post.setAiValidationLastSentAt(Instant.now());

        when(jwtService.getCurrentToken(any())).thenReturn("token");
        when(jwtService.getUsernameFromToken(any())).thenReturn("testuser");
        when(userRepository.findByUsernameAndIsActive(anyString())).thenReturn(Optional.of(currentUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(fanHubRepository.findById(anyLong())).thenReturn(Optional.of(fanHub));

        FanHubMember member = new FanHubMember();
        member.setRoleInHub("MODERATOR");
        when(fanHubMemberRepository.findByHub_IdAndUser_Id(anyLong(), anyLong())).thenReturn(Optional.of(member));

        assertThrows(CooldownException.class, () -> postService.sendAiValidate(1L));
    }

    @Test
    void sendAiValidate_Failure_NotFound() {
        when(jwtService.getCurrentToken(any())).thenReturn("token");
        when(jwtService.getUsernameFromToken(any())).thenReturn("testuser");
        when(userRepository.findByUsernameAndIsActive(anyString())).thenReturn(Optional.of(currentUser));
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.sendAiValidate(1L));
    }

    // --- getPersonalizedFeed tests ---

    @Test
    void getPersonalizedFeed_Unauthenticated() {
        when(authService.getUserFromToken(any())).thenReturn(null);
        Post post = new Post();
        post.setId(1L);
        post.setHub(fanHub);
        post.setUser(currentUser);
        Page<Post> page = new PageImpl<>(List.of(post));
        when(postRepository.findPublicPostsOrderByInteractions(any())).thenReturn(page);

        List<PostResponse> result = postService.getPersonalizedFeed(0, 10, "createdAt");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getPersonalizedFeed_Authenticated_NoFollowedHubs() {
        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(fanHubMemberRepository.findAllByUserId(anyLong())).thenReturn(Collections.emptyList());
        
        Post post = new Post();
        post.setId(1L);
        post.setHub(fanHub);
        post.setUser(currentUser);
        
        when(postRepository.findPublicPosts(anyList(), any())).thenReturn(new PageImpl<>(List.of(post)));

        List<PostResponse> result = postService.getPersonalizedFeed(0, 10, "createdAt");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getPersonalizedFeed_Success() {
        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        FanHubMember member = new FanHubMember();
        member.setHub(fanHub);
        when(fanHubMemberRepository.findAllByUserId(anyLong())).thenReturn(List.of(member));
        
        Post post = new Post();
        post.setId(1L);
        post.setHub(fanHub);
        post.setUser(currentUser);
        
        when(postRepository.findByHubIdInAndStatusApproved(anyList(), any())).thenReturn(new PageImpl<>(List.of(post)));
        when(postRepository.findCategoriesByHubIds(anyList())).thenReturn(List.of("Gaming"));
        when(postRepository.findPublicPostsByCategories(anyList(), anyList(), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        List<PostResponse> result = postService.getPersonalizedFeed(0, 10, "createdAt");

        assertNotNull(result);
    }

    // --- likePost tests ---

    @Test
    void likePost_Success() {
        Post post = new Post();
        post.setId(1L);
        post.setUser(new User());
        post.getUser().setId(2L);
        post.setHub(fanHub);
        post.setTitle("Title");

        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postLikeRepository.findByUserIdAndPostId(anyLong(), anyLong())).thenReturn(Optional.empty());
        
        UserDailyMission mission = new UserDailyMission();
        mission.setLikeAmount(0);
        when(userDailyMissionRepository.findById(anyLong())).thenReturn(Optional.of(mission));

        String result = postService.likePost(1L);

        assertEquals("Post liked successfully!", result);
        verify(postLikeRepository).save(any());
    }

    @Test
    void likePost_Failure_NotFound() {
        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.likePost(1L));
    }

    @Test
    void likePost_Failure_AlreadyLiked() {
        Post post = new Post();
        post.setId(1L);

        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postLikeRepository.findByUserIdAndPostId(anyLong(), anyLong())).thenReturn(Optional.of(new PostLike()));

        assertThrows(IllegalArgumentException.class, () -> postService.likePost(1L));
    }

    @Test
    void likePost_Failure_MissionNotFound() {
        Post post = new Post();
        post.setId(1L);
        post.setUser(new User());
        post.getUser().setId(2L);
        post.setHub(fanHub);

        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postLikeRepository.findByUserIdAndPostId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(userDailyMissionRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.likePost(1L));
    }

    // --- votePost tests ---

    @Test
    void votePost_Success_NewVote() {
        Post post = new Post();
        post.setId(1L);
        post.setPostType("POLL");
        
        VoteOption option = new VoteOption();
        option.setId(1L);
        option.setPost(post);

        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(voteOptionRepository.findById(1L)).thenReturn(Optional.of(option));
        when(postVoteRepository.findByUserIdAndPostId(anyLong(), anyLong())).thenReturn(Collections.emptyList());

        String result = postService.votePost(1L, 1L);

        assertEquals("Vote submitted successfully!", result);
        verify(postVoteRepository).save(any());
    }

    @Test
    void votePost_Success_ChangeVote() {
        Post post = new Post();
        post.setId(1L);
        post.setPostType("POLL");
        
        VoteOption oldOption = new VoteOption();
        oldOption.setId(1L);
        oldOption.setPost(post);
        
        VoteOption newOption = new VoteOption();
        newOption.setId(2L);
        newOption.setPost(post);

        PostVote existingVote = new PostVote();
        existingVote.setOption(oldOption);

        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(voteOptionRepository.findById(2L)).thenReturn(Optional.of(newOption));
        when(postVoteRepository.findByUserIdAndPostId(anyLong(), anyLong())).thenReturn(List.of(existingVote));

        String result = postService.votePost(1L, 2L);

        assertEquals("Vote changed successfully!", result);
        verify(postVoteRepository).save(any());
    }

    @Test
    void votePost_Failure_NotAPoll() {
        Post post = new Post();
        post.setId(1L);
        post.setPostType("TEXT");

        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThrows(IllegalArgumentException.class, () -> postService.votePost(1L, 1L));
    }

    @Test
    void votePost_Failure_OptionNotFound() {
        Post post = new Post();
        post.setId(1L);
        post.setPostType("POLL");

        when(authService.getUserFromToken(any())).thenReturn(currentUser);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(voteOptionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.votePost(1L, 1L));
    }
}
