package com.sep490.vtuber_fanhub.services;

public interface UserDailyMissionService {

    void resetDailyMission();

    void awardPointsForLikes(Long userId, Integer likeAmount);
}
