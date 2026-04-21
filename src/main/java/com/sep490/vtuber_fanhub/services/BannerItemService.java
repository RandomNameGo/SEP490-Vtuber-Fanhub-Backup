package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateBannerItemRequest;
import com.sep490.vtuber_fanhub.dto.requests.GachaBannerItemRequest;
import com.sep490.vtuber_fanhub.dto.responses.BannerItemResponse;
import com.sep490.vtuber_fanhub.dto.responses.GachaResultResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BannerItemService {
    String createBannerItem(CreateBannerItemRequest request, MultipartFile image);

    List<BannerItemResponse> getBannerItemsByBannerId(Long bannerId, int pageNo, int pageSize, String sortBy);

    GachaResultResponse gachaBannerItem(GachaBannerItemRequest request, HttpServletRequest httpRequest);
}
