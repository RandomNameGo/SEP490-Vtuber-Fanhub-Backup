package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.FanHubResponse;
import com.sep490.vtuber_fanhub.dto.responses.SystemAnalyticResponse;
import com.sep490.vtuber_fanhub.dto.responses.UserResponse;
import com.sep490.vtuber_fanhub.models.*;
import com.sep490.vtuber_fanhub.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemAnalyticServiceImpl implements SystemAnalyticService {

    private final UserRepository userRepository;
    private final FanHubRepository fanHubRepository;
    private final FanHubMemberRepository fanHubMemberRepository;
    private final PostRepository postRepository;
    private final FanHubBackgroundRepository fanHubBackgroundRepository;
    private final FanHubCategoryRepository fanHubCategoryRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final PostCommentGiftRepository postCommentGiftRepository;

    @Override
    @Transactional(readOnly = true)
    public SystemAnalyticResponse getSystemAnalytics() {
        // 1. Total user breakdown by role
        List<Object[]> roleCounts = userRepository.countUsersByRole();
        Map<String, Long> userCountByRole = new HashMap<>();
        for (Object[] result : roleCounts) {
            String role = (String) result[0];
            Long count = (Long) result[1];
            userCountByRole.put(role, count);
        }

        long totalFanHub = fanHubRepository.countByIsActiveTrue();

        List<FanHub> topHubs = fanHubRepository.findTopFanHubsByMemberCount(PageRequest.of(0, 5));
        List<FanHubResponse> top5FanHubs = topHubs.stream()
                .map(this::mapToFanHubResponseWithMemberCount)
                .collect(Collectors.toList());

        Instant oneWeekAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        long newUsersInWeek = userRepository.countByCreatedAtAfter(oneWeekAgo);

        List<User> topVtubers = userRepository.findTopVtubersByOshiCount(PageRequest.of(0, 5));
        List<UserResponse> topVtubersByOshi = topVtubers.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());

        List<String> trendingHashtags = postRepository.findTrendingHashtags(PageRequest.of(0, 5))
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        return SystemAnalyticResponse.builder()
                .userCountByRole(userCountByRole)
                .totalFanHub(totalFanHub)
                .top5FanHubs(top5FanHubs)
                .newUsersInWeek(newUsersInWeek)
                .topVtubersByOshi(topVtubersByOshi)
                .trendingHashtags(trendingHashtags)
                .build();
    }

    private FanHubResponse mapToFanHubResponseWithMemberCount(FanHub fanHub) {
        FanHubResponse response = new FanHubResponse();
        response.setFanHubId(fanHub.getId());
        response.setSubdomain(fanHub.getSubdomain());
        response.setHubName(fanHub.getHubName());
        response.setDescription(fanHub.getDescription());
        response.setBannerUrl(fanHub.getBannerUrl());
        
        List<String> backgroundUrls = fanHubBackgroundRepository.findByHubId(fanHub.getId())
                .stream()
                .map(FanHubBackground::getImageUrl)
                .collect(Collectors.toList());
        response.setHighlightImgUrls(backgroundUrls);
        response.setBackgroundUrl(fanHub.getBackgroundUrl());
        
        response.setThemeColor(fanHub.getThemeColor());
        response.setAvatarUrl(fanHub.getAvatarUrl());
        response.setIsPrivate(fanHub.getIsPrivate());
        response.setRequiresApproval(fanHub.getRequiresApproval());
        response.setCreatedAt(fanHub.getCreatedAt());

        User owner = fanHub.getOwnerUser();
        response.setOwnerUserId(owner.getId());
        response.setOwnerUsername(owner.getUsername());
        response.setOwnerDisplayName(owner.getDisplayName());

        List<String> categories = fanHubCategoryRepository.findByHubId(fanHub.getId())
                .stream()
                .map(FanHubCategory::getCategoryName)
                .collect(Collectors.toList());
        response.setCategories(categories);

        long memberCount = fanHubMemberRepository.countJoinedMembers(fanHub.getId());
        response.setMemberCount(memberCount);

        return response;
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setDisplayName(user.getDisplayName());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setFrameUrl(user.getFrameUrl());
        response.setBio(user.getBio());
        response.setRole(user.getRole());
        response.setPoints(user.getPoints());
        response.setPaidPoints(user.getPaidPoints());
        response.setTranslateLanguage(user.getTranslateLanguage());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setIsActive(user.getIsActive());

        if (user.getOshiUser() != null) {
            UserResponse.OshiResponse oshiResponse = new UserResponse.OshiResponse();
            oshiResponse.setUserId(user.getOshiUser().getId());
            oshiResponse.setUsername(user.getOshiUser().getUsername());
            oshiResponse.setDisplayName(user.getOshiUser().getDisplayName());
            oshiResponse.setAvatarUrl(user.getOshiUser().getAvatarUrl());
            response.setOshi(oshiResponse);
        }

        response.setTotalBadges(userBadgeRepository.countByUserId(user.getId()));
        response.setTotalFanHubs(fanHubMemberRepository.countByUserId(user.getId()));
        response.setTotalReceivedGifts(postCommentGiftRepository.countByReceiverId(user.getId()));

        return response;
    }
}
