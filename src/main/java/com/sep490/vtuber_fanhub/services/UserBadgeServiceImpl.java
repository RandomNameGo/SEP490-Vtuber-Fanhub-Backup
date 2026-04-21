package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.models.Badge;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.models.UserBadge;
import com.sep490.vtuber_fanhub.repositories.BadgeRepository;
import com.sep490.vtuber_fanhub.repositories.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserBadgeServiceImpl implements UserBadgeService {

    private final UserBadgeRepository userBadgeRepository;
    private final BadgeRepository badgeRepository;

    @Override
    @Transactional
    public void awardBadge(User user, Long badgeId) {
        if (hasBadge(user, badgeId)) {
            return;
        }

        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("Badge with ID " + badgeId + " not found"));

        UserBadge userBadge = new UserBadge();
        userBadge.setUser(user);
        userBadge.setBadge(badge);
        userBadge.setAcquiredAt(Instant.now());
        userBadge.setIsDisplay(false);

        userBadgeRepository.save(userBadge);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasBadge(User user, Long badgeId) {
        List<UserBadge> userBadges = userBadgeRepository.findByUserId(user.getId());
        return userBadges.stream()
                .anyMatch(ub -> ub.getBadge().getId().equals(badgeId));
    }
}
