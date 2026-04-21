package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.models.UserDailyMission;
import com.sep490.vtuber_fanhub.repositories.UserDailyMissionRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDailyMissionServiceImpl implements UserDailyMissionService {


    private final UserDailyMissionRepository userDailyMissionRepository;
    private final UserRepository userRepository;


    @Override
    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void resetDailyMission() {

        List<UserDailyMission> userDailyMissions = userDailyMissionRepository.findAll();

        for (UserDailyMission userDailyMission : userDailyMissions) {
            userDailyMission.setLikeAmount(0);
            userDailyMission.setBonus10(false);
            userDailyMission.setBonus20(false);
            userDailyMissionRepository.save(userDailyMission);
        }

    }

    @Override
    @Transactional
    public void awardPointsForLikes(Long userId, Integer likeAmount) {
        UserDailyMission mission = userDailyMissionRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User daily mission not found"));

        if (likeAmount >= 10 && !Boolean.TRUE.equals(mission.getBonus10())) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setPoints(user.getPoints() + 20);
            userRepository.save(user);
            mission.setBonus10(true);
            userDailyMissionRepository.save(mission);
        }

        if (likeAmount >= 20 && !Boolean.TRUE.equals(mission.getBonus20())) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setPoints(user.getPoints() + 40);
            userRepository.save(user);
            mission.setBonus20(true);
            userDailyMissionRepository.save(mission);
        }
    }
}
