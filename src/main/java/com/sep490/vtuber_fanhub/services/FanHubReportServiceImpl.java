package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateFanHubReportRequest;
import com.sep490.vtuber_fanhub.dto.responses.FanHubWithReportsResponse;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.FanHub;
import com.sep490.vtuber_fanhub.models.FanHubReport;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.FanHubRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubReportRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FanHubReportServiceImpl implements FanHubReportService {

    private final FanHubReportRepository fanHubReportRepository;
    private final FanHubRepository fanHubRepository;
    private final AuthService authService;
    private final HttpServletRequest httpServletRequest;

    @Override
    @Transactional
    public String createFanHubReport(CreateFanHubReportRequest request) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        FanHub fanHub = fanHubRepository.findById(request.getFanHubId())
                .orElseThrow(() -> new NotFoundException("FanHub not found"));

        FanHubReport report = new FanHubReport();
        report.setFanHub(fanHub);
        report.setReportedBy(currentUser);
        report.setReason(request.getReason());
        report.setStatus("PENDING");
        report.setCreatedAt(Instant.now());

        fanHubReportRepository.save(report);

        return "Report for FanHub created successfully";
    }

    @Override
    @Transactional
    public String bulkResolveFanHubReports(List<Long> reportIds, String resolveMessage) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new CustomAuthenticationException("Only ADMIN can resolve FanHub reports");
        }

        if (reportIds == null || reportIds.isEmpty()) {
            throw new IllegalArgumentException("Report IDs cannot be empty");
        }

        int resolvedCount = 0;
        for (Long reportId : reportIds) {
            Optional<FanHubReport> reportOpt = fanHubReportRepository.findById(reportId);
            if (reportOpt.isEmpty()) {
                continue;
            }

            FanHubReport report = reportOpt.get();
            report.setStatus("RESOLVED");
            report.setResolveMessage(resolveMessage);
            fanHubReportRepository.save(report);
            resolvedCount++;
        }

        return "Successfully resolved " + resolvedCount + " report(s)";
    }

    @Override
    @Transactional(readOnly = true)
    public List<FanHubWithReportsResponse> getAllFanHubsWithReports(int pageNo, int pageSize, String sortBy) {
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, sortBy));
        Page<FanHubReport> reportPage = fanHubReportRepository.findByStatus("PENDING", pageRequest);

        if (reportPage.isEmpty()) {
            return List.of();
        }

        // Group reports by FanHub
        Map<FanHub, List<FanHubReport>> fanHubToReportsMap = reportPage.getContent().stream()
                .collect(Collectors.groupingBy(FanHubReport::getFanHub));

        return fanHubToReportsMap.entrySet().stream()
                .map(entry -> mapToFanHubWithReportsResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private FanHubWithReportsResponse mapToFanHubWithReportsResponse(FanHub fanHub, List<FanHubReport> reports) {
        FanHubWithReportsResponse response = new FanHubWithReportsResponse();
        
        // FanHub information
        response.setFanHubId(fanHub.getId());
        response.setHubName(fanHub.getHubName());
        response.setSubdomain(fanHub.getSubdomain());
        response.setDescription(fanHub.getDescription());
        response.setBannerUrl(fanHub.getBannerUrl());
        response.setAvatarUrl(fanHub.getAvatarUrl());
        response.setIsActive(fanHub.getIsActive());
        response.setIsPrivate(fanHub.getIsPrivate());
        response.setStrikeCount(fanHub.getStrikeCount());
        response.setCreatedAt(fanHub.getCreatedAt());

        // Owner information
        User owner = fanHub.getOwnerUser();
        response.setOwnerUserId(owner.getId());
        response.setOwnerUsername(owner.getUsername());
        response.setOwnerDisplayName(owner.getDisplayName());

        // Convert all reports to SimpleReportResponse
        List<FanHubWithReportsResponse.SimpleReportResponse> reportResponses = reports.stream()
                .map(this::mapToSimpleReportResponse)
                .collect(Collectors.toList());
        response.setReports(reportResponses);

        return response;
    }

    private FanHubWithReportsResponse.SimpleReportResponse mapToSimpleReportResponse(FanHubReport report) {
        FanHubWithReportsResponse.SimpleReportResponse response = new FanHubWithReportsResponse.SimpleReportResponse();

        response.setReportId(report.getId());
        response.setReason(report.getReason());
        response.setReportStatus(report.getStatus());
        response.setReportCreatedAt(report.getCreatedAt());
        response.setResolveMessage(report.getResolveMessage());

        // Reporter information
        User reporter = report.getReportedBy();
        response.setReportedByUserId(reporter.getId());
        response.setReportedByUsername(reporter.getUsername());
        response.setReportedByDisplayName(reporter.getDisplayName());


        return response;
    }
}
