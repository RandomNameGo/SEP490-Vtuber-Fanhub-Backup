package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.PurchaseItemRequest;
import com.sep490.vtuber_fanhub.dto.responses.PurchaseResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.Item;
import com.sep490.vtuber_fanhub.models.ShopItem;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.models.UserItem;
import com.sep490.vtuber_fanhub.repositories.ItemRepository;
import com.sep490.vtuber_fanhub.repositories.ShopItemRepository;
import com.sep490.vtuber_fanhub.repositories.UserItemRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserItemServiceImplTest {

    @Mock
    private UserItemRepository userItemRepository;
    @Mock
    private ShopItemRepository shopItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private AuthService authService;
    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private UserItemServiceImpl userItemService;

    private User user;
    private ShopItem shopItem;
    private Item item;
    private PurchaseItemRequest request;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setPoints(100L);

        item = new Item();
        item.setId(10L);
        item.setItemName("Cool Frame");

        shopItem = new ShopItem();
        shopItem.setId(100L);
        shopItem.setItem(item);
        shopItem.setPrice(50L);

        request = new PurchaseItemRequest();
        request.setShopItemId(100L);
    }

    @Test
    void purchaseItem_Success() {
        // Arrange
        when(authService.getUserFromToken(httpRequest)).thenReturn(user);
        when(shopItemRepository.findById(100L)).thenReturn(Optional.of(shopItem));

        // Act
        PurchaseResponse response = userItemService.purchaseItem(request, httpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(50L, response.getPrice());
        assertEquals(10L, response.getItemId());
        assertEquals(50L, user.getPoints());
        verify(userRepository, times(1)).save(user);
        verify(userItemRepository, times(1)).save(any(UserItem.class));
    }

    @Test
    void purchaseItem_ShopItemNotFound_ThrowsNotFoundException() {
        // Arrange
        when(authService.getUserFromToken(httpRequest)).thenReturn(user);
        when(shopItemRepository.findById(100L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userItemService.purchaseItem(request, httpRequest);
        });

        assertEquals("Shop item not found", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(userItemRepository, never()).save(any(UserItem.class));
    }

    @Test
    void purchaseItem_InsufficientPoints_ThrowsIllegalStateException() {
        // Arrange
        user.setPoints(30L);
        when(authService.getUserFromToken(httpRequest)).thenReturn(user);
        when(shopItemRepository.findById(100L)).thenReturn(Optional.of(shopItem));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            userItemService.purchaseItem(request, httpRequest);
        });

        assertTrue(exception.getMessage().contains("Insufficient points"));
        verify(userRepository, never()).save(any(User.class));
        verify(userItemRepository, never()).save(any(UserItem.class));
    }

    @Test
    void purchaseItem_PointsExactlyEqualToPrice_Success() {
        // Arrange
        user.setPoints(50L);
        when(authService.getUserFromToken(httpRequest)).thenReturn(user);
        when(shopItemRepository.findById(100L)).thenReturn(Optional.of(shopItem));

        // Act
        PurchaseResponse response = userItemService.purchaseItem(request, httpRequest);

        // Assert
        assertEquals(0L, user.getPoints());
        assertNotNull(response);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void purchaseItem_UserPointsNull_TreatedAsZero_ThrowsIllegalStateException() {
        // Arrange
        user.setPoints(null);
        when(authService.getUserFromToken(httpRequest)).thenReturn(user);
        when(shopItemRepository.findById(100L)).thenReturn(Optional.of(shopItem));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            userItemService.purchaseItem(request, httpRequest);
        });

        assertTrue(exception.getMessage().contains("Available: 0"));
    }
}
