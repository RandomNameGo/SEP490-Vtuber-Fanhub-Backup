package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.GachaBannerItemRequest;
import com.sep490.vtuber_fanhub.dto.responses.GachaResultResponse;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BannerItemServiceImplTest {

    @Mock
    private BannerItemRepository bannerItemRepository;
    @Mock
    private BannerRepository bannerRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserItemRepository userItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthService authService;
    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private BannerItemServiceImpl bannerItemService;

    private User user;
    private Banner banner;
    private BannerItem bannerItem1;
    private BannerItem bannerItemGoodLuck;
    private Item item1;
    private GachaBannerItemRequest request;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setPoints(100L);

        banner = new Banner();
        banner.setId(10L);
        banner.setGachaCost(10);
        banner.setStartTime(Instant.now().minus(1, ChronoUnit.DAYS));
        banner.setEndTime(Instant.now().plus(1, ChronoUnit.DAYS));

        item1 = new Item();
        item1.setId(100L);
        item1.setItemName("Rare Item");

        bannerItem1 = new BannerItem();
        bannerItem1.setId(1L);
        bannerItem1.setBanner(banner);
        bannerItem1.setItem(item1);
        bannerItem1.setMultiplier(1);
        bannerItem1.setType("MAIN_REWARD");

        bannerItemGoodLuck = new BannerItem();
        bannerItemGoodLuck.setId(2L);
        bannerItemGoodLuck.setBanner(banner);
        bannerItemGoodLuck.setItem(null);
        bannerItemGoodLuck.setMultiplier(10);
        bannerItemGoodLuck.setType("GOOD_LUCK");

        request = new GachaBannerItemRequest();
        request.setBannerId(10L);
    }

    @Test
    void gachaBannerItem_Success_NewItem() {
        // Arrange
        when(authService.getUserFromToken(httpRequest)).thenReturn(user);
        when(bannerRepository.findById(10L)).thenReturn(Optional.of(banner));
        when(bannerItemRepository.findByBannerId(eq(10L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(bannerItem1)));
        when(userItemRepository.findOwnedItemsByUserAndItems(any(), any())).thenReturn(List.of());

        // Act
        GachaResultResponse response = bannerItemService.gachaBannerItem(request, httpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.getItemId());
        assertEquals(0, response.getPointsRefunded());
        assertEquals(90L, user.getPoints());
        verify(userItemRepository, times(1)).save(any(UserItem.class));
    }

    @Test
    void gachaBannerItem_Success_GoodLuck() {
        // Arrange
        when(authService.getUserFromToken(httpRequest)).thenReturn(user);
        when(bannerRepository.findById(10L)).thenReturn(Optional.of(banner));
        when(bannerItemRepository.findByBannerId(eq(10L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(bannerItemGoodLuck)));

        // Act
        GachaResultResponse response = bannerItemService.gachaBannerItem(request, httpRequest);

        // Assert
        assertNotNull(response);
        assertNull(response.getItemId());
        assertEquals("GOOD_LUCK", response.getType());
        verify(userItemRepository, never()).save(any(UserItem.class));
    }

    @Test
    void gachaBannerItem_Success_DuplicateItem_Refund() {
        // Arrange
        when(authService.getUserFromToken(httpRequest)).thenReturn(user);
        when(bannerRepository.findById(10L)).thenReturn(Optional.of(banner));
        when(bannerItemRepository.findByBannerId(eq(10L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(bannerItem1)));
        when(userItemRepository.findOwnedItemsByUserAndItems(any(), any())).thenReturn(List.of(item1));

        // Act
        GachaResultResponse response = bannerItemService.gachaBannerItem(request, httpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(5, response.getPointsRefunded());
        assertEquals(95L, user.getPoints()); // 100 - 10 + 5
        verify(userItemRepository, never()).save(any(UserItem.class));
    }

    @Test
    void gachaBannerItem_BannerNotFound_ThrowsNotFoundException() {
        // Arrange
        when(authService.getUserFromToken(httpRequest)).thenReturn(user);
        when(bannerRepository.findById(10L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            bannerItemService.gachaBannerItem(request, httpRequest);
        });
    }

    @Test
    void gachaBannerItem_BannerNotActive_ThrowsIllegalStateException() {
        // Arrange
        banner.setEndTime(Instant.now().minus(1, ChronoUnit.HOURS));
        when(authService.getUserFromToken(httpRequest)).thenReturn(user);
        when(bannerRepository.findById(10L)).thenReturn(Optional.of(banner));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            bannerItemService.gachaBannerItem(request, httpRequest);
        });
        assertTrue(exception.getMessage().contains("not currently active"));
    }

    @Test
    void gachaBannerItem_InsufficientPoints_ThrowsIllegalStateException() {
        // Arrange
        user.setPoints(5L);
        when(authService.getUserFromToken(httpRequest)).thenReturn(user);
        when(bannerRepository.findById(10L)).thenReturn(Optional.of(banner));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            bannerItemService.gachaBannerItem(request, httpRequest);
        });
        assertTrue(exception.getMessage().contains("Insufficient points"));
    }
}
