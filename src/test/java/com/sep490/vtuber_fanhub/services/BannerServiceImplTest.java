package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateBannerRequest;
import com.sep490.vtuber_fanhub.models.Banner;
import com.sep490.vtuber_fanhub.repositories.BannerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BannerServiceImplTest {

    @Mock
    private BannerRepository bannerRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private BannerServiceImpl bannerService;

    @Test
    void createBanner_Success() throws IOException {
        CreateBannerRequest request = new CreateBannerRequest();
        request.setName("Test Banner");
        request.setStartTime(Instant.now().plusSeconds(10));
        request.setEndTime(Instant.now().plusSeconds(1000));
        request.setGachaCost(10);

        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(cloudinaryService.uploadFile(image)).thenReturn("http://banner.url");
        when(bannerRepository.findOverlappingBanner(any(), any())).thenReturn(Optional.empty());

        String result = bannerService.createBanner(request, image);

        assertEquals("Created banner successfully", result);
        verify(bannerRepository).save(any(Banner.class));
    }

    @Test
    void createBanner_Failure_InvalidTimeRange() {
        CreateBannerRequest request = new CreateBannerRequest();
        request.setStartTime(Instant.now().plusSeconds(1000));
        request.setEndTime(Instant.now().plusSeconds(10)); // End before start

        assertThrows(IllegalArgumentException.class, () -> {
            bannerService.createBanner(request, null);
        });
    }

    @Test
    void createBanner_Failure_OverlappingBanner() {
        CreateBannerRequest request = new CreateBannerRequest();
        request.setStartTime(Instant.now().plusSeconds(10));
        request.setEndTime(Instant.now().plusSeconds(1000));

        when(bannerRepository.findOverlappingBanner(any(), any())).thenReturn(Optional.of(new Banner()));

        assertThrows(IllegalStateException.class, () -> {
            bannerService.createBanner(request, null);
        });
    }

    @Test
    void createBanner_Failure_UploadError() throws IOException {
        CreateBannerRequest request = new CreateBannerRequest();
        request.setStartTime(Instant.now().plusSeconds(10));
        request.setEndTime(Instant.now().plusSeconds(1000));

        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(cloudinaryService.uploadFile(image)).thenThrow(new IOException("Upload failed"));

        assertThrows(RuntimeException.class, () -> {
            bannerService.createBanner(request, image);
        });
    }

    @Test
    void createBanner_Success_NoImage() {
        CreateBannerRequest request = new CreateBannerRequest();
        request.setName("No Image Banner");
        request.setStartTime(Instant.now().plusSeconds(10));
        request.setEndTime(Instant.now().plusSeconds(1000));

        when(bannerRepository.findOverlappingBanner(any(), any())).thenReturn(Optional.empty());

        String result = bannerService.createBanner(request, null);

        assertEquals("Created banner successfully", result);
        verify(bannerRepository).save(any(Banner.class));
        verifyNoInteractions(cloudinaryService);
    }
}