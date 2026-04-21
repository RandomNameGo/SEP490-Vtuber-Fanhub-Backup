package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreatePostCommentRequest;
import com.sep490.vtuber_fanhub.dto.requests.EditPostCommentRequest;
import com.sep490.vtuber_fanhub.dto.responses.PostCommentResponse;
import com.sep490.vtuber_fanhub.models.PostComment;

import java.util.List;

public interface PostCommentService {

    boolean createPostComment(CreatePostCommentRequest createPostCommentRequest);

    List<PostCommentResponse> getPostCommentsByPostId(Long postId);

    List<PostCommentResponse> getCommentsByParentId(Long parentCommentId);

    String editComment(Long commentId, EditPostCommentRequest request);

    String deleteComment(Long commentId);

    String likeComment(Long commentId);

    String unlikeComment(Long commentId);

    String sendCommentGift(Long commentId);

    String hideComment(Long commentId);

}
