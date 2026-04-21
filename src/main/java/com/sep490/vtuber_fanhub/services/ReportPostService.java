package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateReportPostRequest;
import com.sep490.vtuber_fanhub.dto.responses.ReportPostResponse;
import com.sep490.vtuber_fanhub.dto.responses.PostWithReportsResponse;
import com.sep490.vtuber_fanhub.models.ReportPost;

import java.util.List;

public interface ReportPostService {

    String createReportPost(CreateReportPostRequest createReportPostRequest);

    List<ReportPostResponse> getReportPostsByFanHubId(Long fanHubId, int pageNo, int pageSize, String sortBy);

    String resolveReportPost(Long reportId, String resolveMessage);

    List<ReportPostResponse> getReportPostsByCurrentUser(int pageNo, int pageSize, String sortBy);

    List<ReportPostResponse> getPendingReportPostsByFanHubId(Long fanHubId, int pageNo, int pageSize, String sortBy);

    String bulkResolveReportPosts(List<Long> reportIds, String resolveMessage);

    List<PostWithReportsResponse> getAllPostsWithReports(Long fanHubId, int pageNo, int pageSize, String sortBy);
}
