package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.models.ChatSession;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.ChatSessionRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final HttpServletRequest httpServletRequest;
    @Override
    public Long getExistingChatSessionId() {
        String token = jwtService.getCurrentToken(httpServletRequest);
        String tokenUsername = jwtService.getUsernameFromToken(token);
        Optional<User> tokenUser = userRepository.findByUsernameAndIsActive(tokenUsername);
        if(tokenUser.isEmpty()){
            throw new RuntimeException("Unauthorized User.");
        }

        Optional<ChatSession> session = chatSessionRepository.findByUser_Id(tokenUser.get().getId());
        if(session.isEmpty()){
            return 0L;
        }else return session.get().getId();
    }
}
