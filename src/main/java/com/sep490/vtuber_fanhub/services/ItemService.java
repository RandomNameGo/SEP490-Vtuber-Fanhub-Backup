package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateItemRequest;
import com.sep490.vtuber_fanhub.dto.responses.ItemResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ItemService {
    String createItem(CreateItemRequest request, MultipartFile image);
    List<ItemResponse> getAllFrames();
}
