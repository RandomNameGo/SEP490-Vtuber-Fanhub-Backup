package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreatePostCommentRequest;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.*;
import com.sep490.vtuber_fanhub.repositories.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostCommentServiceImplTest {

    @Mock
    private PostCommentRepository postCommentRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private AuthService authService;
    @Mock
    private PostCommentLikeRepository postCommentLikeRepository;
    @Mock
    private PostCommentGiftRepository postCommentGiftRepository;
    @Mock
    private FanHubMemberRepository fanHubMemberRepository;
    @Mock
    private FanHubRepository fanHubRepository;
    @Mock
    private UserDailyMissionRepository userDailyMissionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserTrackService userTrackService;
    @Mock
    private UserDailyMissionService userDailyMissionService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private BanMemberService banMemberService;

    @InjectMocks
    private PostCommentServiceImpl postCommentService;

    private User currentUser;
    private Post post;
    private FanHub fanHub;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("test_user");
        currentUser.setPoints(10L);

        fanHub = new FanHub();
        fanHub.setId(1L);
        fanHub.setHubName("Test Hub");
        User owner = new User();
        owner.setId(2L);
        fanHub.setOwnerUser(owner);

        post = new Post();
        post.setId(100L);
        post.setHub(fanHub);
        post.setTitle("Test Post");
        User postAuthor = new User();
        postAuthor.setId(3L);
        post.setUser(postAuthor);
    }

    // --- createPostComment ---

    @Test
    void createPostComment_Success_Member() {
        CreatePostCommentRequest request = new CreatePostCommentRequest();
        request.setPostId(100L);
        request.setContent("Nice post!");

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(fanHubMemberRepository.findByHubIdAndUserId(1L, 1L)).thenReturn(Optional.of(new FanHubMember()));

        boolean result = postCommentService.createPostComment(request);

        assertTrue(result);
        verify(postCommentRepository).save(any(PostComment.class));
        verify(userTrackService).updateOnComment(currentUser);
        verify(notificationService).sendPostCommentNotification(anyLong(), anyLong(), anyString(), any(), anyLong(), anyString(), anyLong(), anyString());
    }

    @Test
    void createPostComment_Success_Owner() {
        CreatePostCommentRequest request = new CreatePostCommentRequest();
        request.setPostId(100L);
        request.setContent("Owner comment");

        User owner = fanHub.getOwnerUser();
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(owner);
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        boolean result = postCommentService.createPostComment(request);

        assertTrue(result);
        verify(postCommentRepository).save(any(PostComment.class));
    }

    @Test
    void createPostComment_Failure_PostNotFound() {
        CreatePostCommentRequest request = new CreatePostCommentRequest();
        request.setPostId(999L);

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postCommentService.createPostComment(request));
    }

    @Test
    void createPostComment_Failure_Banned() {
        CreatePostCommentRequest request = new CreatePostCommentRequest();
        request.setPostId(100L);

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        doThrow(new AccessDeniedException("Banned")).when(banMemberService).checkBanStatus(eq(1L), eq(1L), anyList());

        assertThrows(AccessDeniedException.class, () -> postCommentService.createPostComment(request));
    }

    @Test
    void createPostComment_Failure_NotMember() {
        CreatePostCommentRequest request = new CreatePostCommentRequest();
        request.setPostId(100L);

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(fanHubMemberRepository.findByHubIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> postCommentService.createPostComment(request));
    }

    // --- sendCommentGift ---

    @Test
    void sendCommentGift_Success() {
        PostComment comment = new PostComment();
        comment.setId(500L);
        User receiver = new User();
        receiver.setId(10L);
        receiver.setPoints(5L);
        comment.setUser(receiver);

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(postCommentRepository.findById(500L)).thenReturn(Optional.of(comment));
        when(postCommentGiftRepository.findBySenderAndComment(eq(currentUser), eq(comment))).thenReturn(Optional.empty());

        String result = postCommentService.sendCommentGift(500L);

        assertEquals("Gift sent successfully!", result);
        assertEquals(8L, currentUser.getPoints()); // 10 - 2
        assertEquals(7L, receiver.getPoints()); // 5 + 2
        verify(userRepository, times(2)).save(any(User.class));
        verify(postCommentGiftRepository).save(any(PostCommentGift.class));
    }

    @Test
    void sendCommentGift_Failure_CommentNotFound() {
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(postCommentRepository.findById(500L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postCommentService.sendCommentGift(500L));
    }

    @Test
    void sendCommentGift_Failure_SendToSelf() {
        PostComment comment = new PostComment();
        comment.setId(500L);
        comment.setUser(currentUser); // Same as sender

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(postCommentRepository.findById(500L)).thenReturn(Optional.of(comment));

        assertThrows(IllegalArgumentException.class, () -> postCommentService.sendCommentGift(500L));
    }

    @Test
    void sendCommentGift_Failure_InsufficientPoints() {
        currentUser.setPoints(1L); // Less than 2
        PostComment comment = new PostComment();
        comment.setId(500L);
        User receiver = new User();
        receiver.setId(10L);
        comment.setUser(receiver);

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(postCommentRepository.findById(500L)).thenReturn(Optional.of(comment));

        assertThrows(IllegalArgumentException.class, () -> postCommentService.sendCommentGift(500L));
    }

    @Test
    void sendCommentGift_Success_ExistingGift() {
        PostComment comment = new PostComment();
        comment.setId(500L);
        User receiver = new User();
        receiver.setId(10L);
        receiver.setPoints(5L);
        comment.setUser(receiver);

        PostCommentGift existingGift = new PostCommentGift();
        existingGift.setAmount(2L);

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(postCommentRepository.findById(500L)).thenReturn(Optional.of(comment));
        when(postCommentGiftRepository.findBySenderAndComment(eq(currentUser), eq(comment))).thenReturn(Optional.of(existingGift));

        String result = postCommentService.sendCommentGift(500L);

        assertEquals("Gift sent successfully!", result);
        assertEquals(4L, existingGift.getAmount()); // 2 + 2
        verify(postCommentGiftRepository).save(existingGift);
    }
}
