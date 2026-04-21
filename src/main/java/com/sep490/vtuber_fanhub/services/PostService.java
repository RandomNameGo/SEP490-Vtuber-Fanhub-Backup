package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreatePollPostRequest;
import com.sep490.vtuber_fanhub.dto.requests.CreatePostRequest;
import com.sep490.vtuber_fanhub.dto.responses.SummarizePostResponse;
import com.sep490.vtuber_fanhub.dto.responses.PostResponse;
import com.sep490.vtuber_fanhub.dto.responses.TranslatePostResponse;
import com.sep490.vtuber_fanhub.dto.responses.PostWithMediaResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {

    String createPost(CreatePostRequest request, List<MultipartFile> images, MultipartFile video);

    String createPollPost(CreatePollPostRequest request);

    List<PostWithMediaResponse> getPendingPosts(Long fanHubId, int pageNo, int pageSize, String sortBy);

    List<PostResponse> getPosts(Long fanHubId, int pageNo, int pageSize, String sortBy, String postHashtag, String authorUsername);

    List<PostResponse> getPostsBySubdomain(String subdomain, int pageNo, int pageSize, String sortBy, String postHashtag, String authorUsername);

    List<PostWithMediaResponse> getPendingPostsBySubdomain(String subdomain, int pageNo, int pageSize, String sortBy);

    List<PostResponse> getAnnouncementAndEventPosts(Long fanHubId, int pageNo, int pageSize, String sortBy);

    String reviewPost(Long postId, String status);

    String reviewPosts(List<Long> postIds, String status);

    String sendAiValidate(Long postId);

    List<PostResponse> getPersonalizedFeed(int pageNo, int pageSize, String sortBy);

    TranslatePostResponse translatePost(Long postId);

    SummarizePostResponse summarizePost(Long postId);
    String rejectPost(Long postId, String reason);

    String likePost(Long postId);

    String unlikePost(Long postId);

    String pinPost(Long postId);

    String unpinPost(Long postId);

    String votePost(Long postId, Long optionId);

    String unVotePost(Long postId);

    String deletePost(Long postId);

    List<PostWithMediaResponse> getBookmarkPosts(int pageNo, int pageSize, String sortBy);

    List<PostWithMediaResponse> getPostsByUsername(int pageNo, int pageSize, String sortBy);

    List<PostWithMediaResponse> getAllPostsByFanHubId(Long fanHubId, int pageNo, int pageSize, String sortBy);

    List<PostResponse> getAllPostsBySubdomain(String subdomain, int pageNo, int pageSize, String sortBy);

    PostResponse getPostDetail(Long postId);

    PostResponse getApprovedPostById(Long postId);

    String approveAiSafePosts(Long fanHubId);

    String rejectAiUnsafePosts(Long fanHubId);

    List<PostResponse> getTrendingPostsByFanHub(Long fanHubId, int pageNo, int pageSize, String sortBy);

    PostResponse getTrendingPublicPost();

    PostResponse getLatestPublicApprovedPost();

    List<PostResponse> searchPosts(String keyword, int pageNo, int pageSize, String sortBy);
}
