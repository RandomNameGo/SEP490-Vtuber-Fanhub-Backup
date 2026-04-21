package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateFanHubReportRequest;
import com.sep490.vtuber_fanhub.dto.responses.FanHubWithReportsResponse;

import java.util.List;

public interface FanHubReportService {

    String createFanHubReport(CreateFanHubReportRequest request);

    String bulkResolveFanHubReports(List<Long> reportIds, String resolveMessage);

    List<FanHubWithReportsResponse> getAllFanHubsWithReports(int pageNo, int pageSize, String sortBy);
}
