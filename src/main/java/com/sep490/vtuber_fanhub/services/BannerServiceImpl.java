package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateBannerRequest;
import com.sep490.vtuber_fanhub.dto.responses.BannerResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.Banner;
import com.sep490.vtuber_fanhub.repositories.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;

    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public String createBanner(CreateBannerRequest request, MultipartFile bannerImage) {
        // Validate that end time is after start time
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // Check if there's an overlapping active banner
        Optional<Banner> overlappingBanner = bannerRepository.findOverlappingBanner(
                request.getStartTime(), request.getEndTime());
        if (overlappingBanner.isPresent()) {
            throw new IllegalStateException("Another banner is already active during this time period");
        }

        // Upload banner image if provided
        String bannerImgUrl = null;
        if (bannerImage != null && !bannerImage.isEmpty()) {
            try {
                bannerImgUrl = cloudinaryService.uploadFile(bannerImage);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload banner image", e);
            }
        }

        Banner banner = new Banner();
        banner.setName(request.getName());
        banner.setStartTime(request.getStartTime());
        banner.setEndTime(request.getEndTime());
        banner.setDescription(request.getDescription());
        banner.setGachaCost(request.getGachaCost());
        banner.setCreatedAt(Instant.now());
        banner.setBannerImgUrl(bannerImgUrl);
        banner.setIsActive(true);

        bannerRepository.save(banner);

        return "Created banner successfully";
    }

    @Override
    @Transactional(readOnly = true)
    public List<BannerResponse> getAllBanners(int pageNo, int pageSize, String sortBy) {
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));

        Page<Banner> pagedBanners = bannerRepository.findAll(paging);

        if (pagedBanners.isEmpty()) {
            return List.of();
        }

        return pagedBanners.getContent().stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BannerResponse getActiveBanner() {
        Instant now = Instant.now();
        Banner banner = bannerRepository.findActiveBanner(now)
                .orElseThrow(() -> new NotFoundException("No active banner found"));

        return convertToResponse(banner);
    }

    private BannerResponse convertToResponse(Banner banner) {
        BannerResponse response = new BannerResponse();
        response.setBannerId(banner.getId());
        response.setName(banner.getName());
        response.setStartTime(banner.getStartTime());
        response.setEndTime(banner.getEndTime());
        response.setDescription(banner.getDescription());
        response.setGachaCost(banner.getGachaCost());
        response.setCreatedAt(banner.getCreatedAt());
        response.setBannerImgUrl(banner.getBannerImgUrl());
        response.setIsActive(banner.getIsActive());
        return response;
    }
}
