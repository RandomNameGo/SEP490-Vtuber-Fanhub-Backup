package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateFanHubStrikeRequest;
import com.sep490.vtuber_fanhub.dto.responses.FanHubStrikeResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.FanHub;
import com.sep490.vtuber_fanhub.models.FanHubStrike;
import com.sep490.vtuber_fanhub.models.SystemAccount;
import com.sep490.vtuber_fanhub.repositories.FanHubRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubStrikeRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class FanHubStrikeServiceImpl implements FanHubStrikeService {

    private final FanHubStrikeRepository fanHubStrikeRepository;
    private final FanHubRepository fanHubRepository;
    private final AuthService authService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public FanHubStrikeResponse createStrike(CreateFanHubStrikeRequest request, HttpServletRequest httpServletRequest) {
        SystemAccount strikeBy = authService.getSystemAccountFromToken(httpServletRequest);

        FanHub hub = fanHubRepository.findById(request.getFanHubId())
                .orElseThrow(() -> new NotFoundException("FanHub not found"));

        // Create the strike
        FanHubStrike strike = new FanHubStrike();
        strike.setHub(hub);
        strike.setReason(request.getReason());
        strike.setStrikeBy(strikeBy);
        strike.setCreatedAt(Instant.now());
        strike.setIsActive(true);

        strike = fanHubStrikeRepository.save(strike);

        // Update hub strike count
        int currentStrikes = hub.getStrikeCount() != null ? hub.getStrikeCount() : 0;
        hub.setStrikeCount(currentStrikes + 1);
        fanHubRepository.save(hub);

        // Send notification to hub owner
        notificationService.sendFanHubStrikeNotification(
                hub.getOwnerUser().getId(),
                hub.getId(),
                hub.getHubName(),
                hub.getStrikeCount(),
                request.getReason()
        );

        // Prepare response
        FanHubStrikeResponse response = new FanHubStrikeResponse();
        response.setId(strike.getId());
        response.setFanHubId(hub.getId());
        response.setFanHubName(hub.getHubName());
        response.setReason(strike.getReason());
        response.setStrikeBySystemAccountId(strikeBy.getId());
        response.setStrikeByUsername(strikeBy.getUsername());
        response.setCreatedAt(strike.getCreatedAt());
        response.setIsActive(strike.getIsActive());

        return response;
    }
}
