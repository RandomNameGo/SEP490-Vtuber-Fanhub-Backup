package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateBannerRequest;
import com.sep490.vtuber_fanhub.dto.responses.BannerResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BannerService {
    String createBanner(CreateBannerRequest request, MultipartFile bannerImage);

    List<BannerResponse> getAllBanners(int pageNo, int pageSize, String sortBy);

    BannerResponse getActiveBanner();
}
