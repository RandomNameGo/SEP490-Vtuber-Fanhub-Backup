package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateFanHubRequest;
import com.sep490.vtuber_fanhub.dto.requests.UpdateFanHubRequest;
import com.sep490.vtuber_fanhub.dto.responses.FanHubAnalyticsResponse;
import com.sep490.vtuber_fanhub.dto.responses.FanHubResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FanHubService {

    String createFanHub(CreateFanHubRequest request);

    String createFanHubV2(CreateFanHubRequest request, MultipartFile banner, List<MultipartFile> backgrounds, MultipartFile avatar) throws IOException;

    String updateFanHub(Long fanHubId, UpdateFanHubRequest request);

    String uploadFanHubBannerBackGroundAvatar(long fanHubId, MultipartFile banner, List<MultipartFile> highlight, MultipartFile avatar) throws IOException;

    List<FanHubResponse> getAllFanHubs(int pageNo, int pageSize, String sortBy, boolean includePrivate);

    List<FanHubResponse> getTopFanHubs(int pageNo, int pageSize, String category);

    FanHubResponse getFanHubBySubdomain(String subdomain);

    List<FanHubResponse> getJoinedFanHubs(int pageNo, int pageSize, String sortBy);

    FanHubResponse getMyHubAsOwner();

    FanHubAnalyticsResponse getFanHubAnalytics(Long fanHubId);

    String deleteFanHub(Long fanHubId);

    String deactivateFanHub(Long fanHubId);

    List<FanHubResponse> searchFanHubs(String keyword, int pageNo, int pageSize, String sortBy);
}
