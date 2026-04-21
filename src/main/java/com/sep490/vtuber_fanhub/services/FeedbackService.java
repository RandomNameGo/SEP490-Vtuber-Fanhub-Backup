package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateFeedbackRequest;
import com.sep490.vtuber_fanhub.dto.responses.FeedbackResponse;
import com.sep490.vtuber_fanhub.models.FeedbackCategory;

import java.util.List;

public interface FeedbackService {

    String submitFeedback(CreateFeedbackRequest request);

    List<FeedbackResponse> getAllFeedback(int pageNo, int pageSize, String sortBy);

    List<FeedbackResponse> getCurrentUserFeedback(int pageNo, int pageSize, String sortBy);

    FeedbackResponse getFeedbackDetail(Long id);

    List<FeedbackCategory> getAllFeedbackCategories();
}
