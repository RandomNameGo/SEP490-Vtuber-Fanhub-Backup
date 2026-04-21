package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.models.UserTrack;
import com.sep490.vtuber_fanhub.repositories.UserTrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserTrackServiceImpl implements UserTrackService {

    private final UserTrackRepository userTrackRepository;
    private final UserBadgeService userBadgeService;

    @Override
    @Transactional
    public void updateOnLike(User user) {
        Optional<UserTrack> existingTrack = userTrackRepository.findByUserId(user.getId());

        Long newMaxLikes;
        if (existingTrack.isPresent()) {
            UserTrack track = existingTrack.get();
            Long currentMaxLikes = track.getMaxLikes() != null ? track.getMaxLikes() : 0L;
            newMaxLikes = currentMaxLikes + 1;
            track.setMaxLikes(newMaxLikes);
            userTrackRepository.save(track);
        } else {
            UserTrack track = new UserTrack();
            track.setUser(user);
            track.setMaxLikes(1L);
            track.setMaxComments(0L);
            userTrackRepository.save(track);
            newMaxLikes = 1L;
        }

        // Award badge ID 7 for first like (max likes = 1)
        if (newMaxLikes == 1) {
            userBadgeService.awardBadge(user, 7L);
        }
        // Award badge ID 8 for reaching 100 likes (max likes = 100)
        else if (newMaxLikes == 100) {
            userBadgeService.awardBadge(user, 8L);
        }
    }

    @Override
    @Transactional
    public void updateOnComment(User user) {
        Optional<UserTrack> existingTrack = userTrackRepository.findByUserId(user.getId());

        if (existingTrack.isPresent()) {
            UserTrack track = existingTrack.get();
            Long currentMaxComments = track.getMaxComments() != null ? track.getMaxComments() : 0L;
            track.setMaxComments(currentMaxComments + 1);
            userTrackRepository.save(track);
        } else {
            UserTrack track = new UserTrack();
            track.setUser(user);
            track.setMaxLikes(0L);
            track.setMaxComments(1L);
            userTrackRepository.save(track);
        }
    }
}
