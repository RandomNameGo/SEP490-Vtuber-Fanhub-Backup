package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateFanHubRequest;
import com.sep490.vtuber_fanhub.dto.requests.UpdateFanHubRequest;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.FanHub;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FanHubServiceImplTest {

    @Mock
    private FanHubRepository fanHubRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private FanHubCategoryRepository fanHubCategoryRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private AuthService authService;

    @Mock
    private FanHubMemberRepository fanHubMemberRepository;

    @Mock
    private FanHubBackgroundRepository fanHubBackgroundRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private FanHubStrikeRepository fanHubStrikeRepository;

    @InjectMocks
    private FanHubServiceImpl fanHubService;

    private User vtuberUser;
    private User normalUser;
    private CreateFanHubRequest createRequest;
    private FanHub existingHub;

    @BeforeEach
    void setUp() {
        vtuberUser = new User();
        vtuberUser.setId(1L);
        vtuberUser.setUsername("vtuber");
        vtuberUser.setRole("VTUBER");

        normalUser = new User();
        normalUser.setId(2L);
        normalUser.setUsername("user");
        normalUser.setRole("USER");

        createRequest = new CreateFanHubRequest();
        createRequest.setHubName("Test Hub");
        createRequest.setSubdomain("test");
        createRequest.setCategory(Arrays.asList("Category1", "Category2"));

        existingHub = new FanHub();
        existingHub.setId(10L);
        existingHub.setOwnerUser(vtuberUser);
        existingHub.setSubdomain("test");
        existingHub.setIsActive(true);
    }

    @Test
    void createFanHubV2_Success() throws IOException {
        MultipartFile banner = mock(MultipartFile.class);
        MultipartFile avatar = mock(MultipartFile.class);
        List<MultipartFile> backgrounds = Arrays.asList(mock(MultipartFile.class));

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(vtuberUser);
        when(userRepository.findByUsernameAndIsActive("vtuber")).thenReturn(Optional.of(vtuberUser));
        when(banner.isEmpty()).thenReturn(false);
        when(avatar.isEmpty()).thenReturn(false);
        when(backgrounds.get(0).isEmpty()).thenReturn(false);
        when(cloudinaryService.uploadFile(any())).thenReturn("http://url.com");

        String result = fanHubService.createFanHubV2(createRequest, banner, backgrounds, avatar);

        assertEquals("Created FanHub successfully", result);
        verify(fanHubRepository, times(2)).save(any(FanHub.class));
        verify(fanHubCategoryRepository, times(2)).save(any());
        verify(fanHubBackgroundRepository, times(1)).save(any());
    }

    @Test
    void createFanHubV2_NoFiles() throws IOException {
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(vtuberUser);
        when(userRepository.findByUsernameAndIsActive("vtuber")).thenReturn(Optional.of(vtuberUser));

        String result = fanHubService.createFanHubV2(createRequest, null, null, null);

        assertEquals("Created FanHub successfully", result);
        verify(fanHubRepository).save(any(FanHub.class));
    }

    @Test
    void createFanHubV2_NotVtuber() throws IOException {
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(normalUser);
        when(userRepository.findByUsernameAndIsActive("user")).thenReturn(Optional.of(normalUser));

        assertThrows(AccessDeniedException.class, () -> 
            fanHubService.createFanHubV2(createRequest, null, null, null));
    }

    @Test
    void createFanHubV2_TooManyBackgrounds() throws IOException {
        List<MultipartFile> backgrounds = Arrays.asList(
            mock(MultipartFile.class), mock(MultipartFile.class), 
            mock(MultipartFile.class), mock(MultipartFile.class), 
            mock(MultipartFile.class)
        );

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(vtuberUser);
        when(userRepository.findByUsernameAndIsActive("vtuber")).thenReturn(Optional.of(vtuberUser));

        assertThrows(IllegalArgumentException.class, () -> 
            fanHubService.createFanHubV2(createRequest, null, backgrounds, null));
    }

    @Test
    void createFanHubV2_TokenInvalid() {
        when(authService.getUserFromToken(httpServletRequest)).thenThrow(new CustomAuthenticationException("Invalid token"));

        assertThrows(CustomAuthenticationException.class, () -> 
            fanHubService.createFanHubV2(createRequest, null, null, null));
    }

    @Test
    void updateFanHub_Success() {
        UpdateFanHubRequest updateRequest = new UpdateFanHubRequest();
        updateRequest.setHubName("Updated Name");
        updateRequest.setCategory(Collections.singletonList("NewCat"));

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(vtuberUser);
        when(fanHubRepository.findByIdAndIsActive(10L, true)).thenReturn(Optional.of(existingHub));

        String result = fanHubService.updateFanHub(10L, updateRequest);

        assertEquals("Updated FanHub successfully", result);
        assertEquals("Updated Name", existingHub.getHubName());
        verify(fanHubCategoryRepository).deleteByHubId(10L);
        verify(fanHubCategoryRepository).save(any());
        verify(fanHubRepository).save(existingHub);
    }

    @Test
    void updateFanHub_PartialFields() {
        UpdateFanHubRequest updateRequest = new UpdateFanHubRequest();
        updateRequest.setDescription("New Description");

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(vtuberUser);
        when(fanHubRepository.findByIdAndIsActive(10L, true)).thenReturn(Optional.of(existingHub));

        String result = fanHubService.updateFanHub(10L, updateRequest);

        assertEquals("Updated FanHub successfully", result);
        assertEquals("New Description", existingHub.getDescription());
        verify(fanHubRepository).save(existingHub);
    }

    @Test
    void updateFanHub_NotFound() {
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(vtuberUser);
        when(fanHubRepository.findByIdAndIsActive(99L, true)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> 
            fanHubService.updateFanHub(99L, new UpdateFanHubRequest()));
    }

    @Test
    void updateFanHub_NotOwner() {
        User otherVtuber = new User();
        otherVtuber.setId(3L);
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(otherVtuber);
        when(fanHubRepository.findByIdAndIsActive(10L, true)).thenReturn(Optional.of(existingHub));

        assertThrows(CustomAuthenticationException.class, () -> 
            fanHubService.updateFanHub(10L, new UpdateFanHubRequest()));
    }

    @Test
    void updateFanHub_SubdomainInUse() {
        UpdateFanHubRequest updateRequest = new UpdateFanHubRequest();
        updateRequest.setSubdomain("alreadytaken");

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(vtuberUser);
        when(fanHubRepository.findByIdAndIsActive(10L, true)).thenReturn(Optional.of(existingHub));
        when(fanHubRepository.existsBySubdomainAndIsActive("alreadytaken", true)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> 
            fanHubService.updateFanHub(10L, updateRequest));
    }
}
