package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateItemRequest;
import com.sep490.vtuber_fanhub.dto.responses.ItemResponse;
import com.sep490.vtuber_fanhub.models.Item;
import com.sep490.vtuber_fanhub.repositories.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public String createItem(CreateItemRequest request, MultipartFile image) {
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                imageUrl = cloudinaryService.uploadFile(image);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image", e);
            }
        }

        Item item = new Item();
        item.setItemName(request.getItemName());
        item.setDescription(request.getDescription());
        item.setImageUrl(imageUrl);
        item.setCategory(request.getCategory());

        itemRepository.save(item);

        return "Created item successfully";
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllFrames() {
        return itemRepository.findByCategory("FRAME").stream()
                .map(this::mapToItemResponse)
                .collect(Collectors.toList());
    }

    private ItemResponse mapToItemResponse(Item item) {
        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setItemName(item.getItemName());
        response.setDescription(item.getDescription());
        response.setImageUrl(item.getImageUrl());
        response.setCategory(item.getCategory());
        return response;
    }
}
