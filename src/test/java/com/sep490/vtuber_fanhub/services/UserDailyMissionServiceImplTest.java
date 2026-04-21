package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.models.UserDailyMission;
import com.sep490.vtuber_fanhub.repositories.UserDailyMissionRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserDailyMissionServiceImplTest {

    @Mock
    private UserDailyMissionRepository userDailyMissionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDailyMissionServiceImpl userDailyMissionService;

    private User user;
    private UserDailyMission mission;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(userId);
        user.setPoints(100L);

        mission = new UserDailyMission();
        mission.setUser(user);
        mission.setLikeAmount(0);
        mission.setBonus10(false);
        mission.setBonus20(false);
    }

    @Test
    void awardPointsForLikes_10Likes_AwardsBonus10() {
        // Arrange
        when(userDailyMissionRepository.findById(userId)).thenReturn(Optional.of(mission));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        userDailyMissionService.awardPointsForLikes(userId, 10);

        // Assert
        assertEquals(120L, user.getPoints());
        assertTrue(mission.getBonus10());
        assertFalse(mission.getBonus20());
        verify(userRepository, times(1)).save(user);
        verify(userDailyMissionRepository, times(1)).save(mission);
    }

    @Test
    void awardPointsForLikes_20Likes_AwardsBothBonuses() {
        // Arrange
        when(userDailyMissionRepository.findById(userId)).thenReturn(Optional.of(mission));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        userDailyMissionService.awardPointsForLikes(userId, 20);

        // Assert
        assertEquals(160L, user.getPoints()); // 100 + 20 + 40
        assertTrue(mission.getBonus10());
        assertTrue(mission.getBonus20());
        verify(userRepository, times(2)).save(user);
        verify(userDailyMissionRepository, times(2)).save(mission);
    }

    @Test
    void awardPointsForLikes_20Likes_AlreadyAwarded10_AwardsOnlyBonus20() {
        // Arrange
        mission.setBonus10(true);
        when(userDailyMissionRepository.findById(userId)).thenReturn(Optional.of(mission));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        userDailyMissionService.awardPointsForLikes(userId, 20);

        // Assert
        assertEquals(140L, user.getPoints()); // 100 + 40
        assertTrue(mission.getBonus10());
        assertTrue(mission.getBonus20());
        verify(userRepository, times(1)).save(user);
        verify(userDailyMissionRepository, times(1)).save(mission);
    }

    @Test
    void awardPointsForLikes_5Likes_NoBonusAwarded() {
        // Arrange
        when(userDailyMissionRepository.findById(userId)).thenReturn(Optional.of(mission));

        // Act
        userDailyMissionService.awardPointsForLikes(userId, 5);

        // Assert
        assertEquals(100L, user.getPoints());
        assertFalse(mission.getBonus10());
        assertFalse(mission.getBonus20());
        verify(userRepository, never()).save(any());
        verify(userDailyMissionRepository, never()).save(any());
    }

    @Test
    void awardPointsForLikes_MissionNotFound_ThrowsRuntimeException() {
        // Arrange
        when(userDailyMissionRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userDailyMissionService.awardPointsForLikes(userId, 10);
        });
        assertEquals("User daily mission not found", exception.getMessage());
    }

    @Test
    void awardPointsForLikes_UserNotFound_ThrowsRuntimeException() {
        // Arrange
        when(userDailyMissionRepository.findById(userId)).thenReturn(Optional.of(mission));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userDailyMissionService.awardPointsForLikes(userId, 10);
        });
        assertEquals("User not found", exception.getMessage());
    }
}
