package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.PurchaseItemRequest;
import com.sep490.vtuber_fanhub.dto.responses.PurchaseResponse;
import com.sep490.vtuber_fanhub.dto.responses.UserItemResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserItemService {

    PurchaseResponse purchaseItem(PurchaseItemRequest request, HttpServletRequest httpRequest);

    List<UserItemResponse> getItemsByCurrentUser(HttpServletRequest httpRequest, int pageNo, int pageSize, String sortBy);

    List<UserItemResponse> getMyFrames(HttpServletRequest httpRequest);
}
