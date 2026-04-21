package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.models.User;

public interface UserBadgeService {
    /**
     * Award a badge to a user if they don't already have it
     *
     * @param user    the user to award the badge to
     * @param badgeId the ID of the badge to award
     */
    void awardBadge(User user, Long badgeId);

    /**
     * Check if a user already has a specific badge
     * @param user the user to check
     * @param badgeId the ID of the badge to check
     * @return true if user has the badge, false otherwise
     */
    boolean hasBadge(User user, Long badgeId);
}
