package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateShopItemRequest;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.Item;
import com.sep490.vtuber_fanhub.models.ShopItem;
import com.sep490.vtuber_fanhub.repositories.ItemRepository;
import com.sep490.vtuber_fanhub.repositories.ShopItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShopItemServiceImplTest {

    @Mock
    private ShopItemRepository shopItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private ShopItemServiceImpl shopItemService;

    @Test
    void createShopItem_Success_NewItem() throws IOException {
        // Arrange
        CreateShopItemRequest request = new CreateShopItemRequest();
        request.setItemName("New Item");
        request.setDescription("Description");
        request.setCategory("Category");
        request.setPrice(100L);
        request.setItemId(null);

        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(cloudinaryService.uploadFile(image)).thenReturn("http://image.url");

        // Act
        String result = shopItemService.createShopItem(request, image);

        // Assert
        assertEquals("Created shop item successfully", result);
        verify(itemRepository).save(any(Item.class));
        verify(shopItemRepository).save(any(ShopItem.class));
        verify(cloudinaryService).uploadFile(image);
    }

    @Test
    void createShopItem_Success_ExistingItem() throws IOException {
        // Arrange
        CreateShopItemRequest request = new CreateShopItemRequest();
        request.setItemId(1L);
        request.setPrice(200L);

        Item existingItem = new Item();
        existingItem.setId(1L);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem));

        // Act
        String result = shopItemService.createShopItem(request, null);

        // Assert
        assertEquals("Created shop item successfully", result);
        verify(itemRepository, never()).save(any(Item.class));
        verify(shopItemRepository).save(any(ShopItem.class));
    }

    @Test
    void createShopItem_Failure_ItemNotFound() {
        // Arrange
        CreateShopItemRequest request = new CreateShopItemRequest();
        request.setItemId(99L);

        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            shopItemService.createShopItem(request, null);
        });
        verify(shopItemRepository, never()).save(any());
    }

    @Test
    void createShopItem_Failure_ImageUploadError() throws IOException {
        // Arrange
        CreateShopItemRequest request = new CreateShopItemRequest();
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(cloudinaryService.uploadFile(image)).thenThrow(new IOException("Upload failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            shopItemService.createShopItem(request, image);
        });
        assertEquals("Failed to upload image", exception.getMessage());
    }

    @Test
    void createShopItem_Success_NoImage() {
        // Arrange
        CreateShopItemRequest request = new CreateShopItemRequest();
        request.setItemName("No Image Item");
        request.setPrice(50L);

        // Act
        String result = shopItemService.createShopItem(request, null);

        // Assert
        assertEquals("Created shop item successfully", result);
        verify(itemRepository).save(any(Item.class));
        verify(shopItemRepository).save(any(ShopItem.class));
        verifyNoInteractions(cloudinaryService);
    }
}
