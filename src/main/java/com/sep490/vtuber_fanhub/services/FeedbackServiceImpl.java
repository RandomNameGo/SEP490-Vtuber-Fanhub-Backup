package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateFeedbackRequest;
import com.sep490.vtuber_fanhub.dto.responses.FeedbackResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.FeedbackCategory;
import com.sep490.vtuber_fanhub.models.SystemAccount;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.models.UserFeedback;
import com.sep490.vtuber_fanhub.repositories.FeedbackCategoryRepository;
import com.sep490.vtuber_fanhub.repositories.UserFeedbackRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final UserFeedbackRepository userFeedbackRepository;

    private final FeedbackCategoryRepository feedbackCategoryRepository;

    private final AuthService authService;

    private final HttpServletRequest httpServletRequest;

    @Override
    public String submitFeedback(CreateFeedbackRequest request) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FeedbackCategory> categoryOpt = feedbackCategoryRepository.findById(request.getCategoryId());
        if (categoryOpt.isEmpty()) {
            throw new NotFoundException("Feedback category not found");
        }

        UserFeedback feedback = new UserFeedback();
        feedback.setCategory(categoryOpt.get());
        feedback.setUser(currentUser);
        feedback.setContent(request.getContent());
        feedback.setCreatedAt(Instant.now());
        feedback.setIsRead(false);
        userFeedbackRepository.save(feedback);

        return "Feedback submitted successfully";
    }

    @Override
    public List<FeedbackResponse> getAllFeedback(int pageNo, int pageSize, String sortBy) {
        SystemAccount currentUser = authService.getSystemAccountFromToken(httpServletRequest);

        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, sortBy));
        Page<UserFeedback> feedbackPage = userFeedbackRepository.findAll(pageRequest);

        return feedbackPage.getContent().stream()
                .map(this::mapToFeedbackResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FeedbackResponse> getCurrentUserFeedback(int pageNo, int pageSize, String sortBy) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, sortBy));
        Page<UserFeedback> feedbackPage = userFeedbackRepository.findByUserId(currentUser.getId(), pageRequest);

        return feedbackPage.getContent().stream()
                .map(this::mapToFeedbackResponse)
                .collect(Collectors.toList());
    }

    @Override
    public FeedbackResponse getFeedbackDetail(Long id) {
        User currentUser = authService.getUserFromToken(httpServletRequest);
        UserFeedback feedback = userFeedbackRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Feedback not found or you don't have permission to access it"));

        return mapToFeedbackResponse(feedback);
    }



    @Override
    public List<FeedbackCategory> getAllFeedbackCategories() {
        return feedbackCategoryRepository.findAll();
    }

    private FeedbackResponse mapToFeedbackResponse(UserFeedback feedback) {
        FeedbackResponse response = new FeedbackResponse();

        response.setFeedbackId(feedback.getId());
        response.setCategoryId(feedback.getCategory().getId());
        response.setCategoryName(feedback.getCategory().getCategoryName());
        response.setUserId(feedback.getUser().getId());
        response.setUsername(feedback.getUser().getUsername());
        response.setDisplayName(feedback.getUser().getDisplayName());
        response.setContent(feedback.getContent());
        response.setCreatedAt(feedback.getCreatedAt());
        response.setIsRead(feedback.getIsRead());

        return response;
    }
}
