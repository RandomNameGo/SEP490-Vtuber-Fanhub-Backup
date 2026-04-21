package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateReportMemberRequest;
import com.sep490.vtuber_fanhub.dto.responses.ReportMemberResponse;
import com.sep490.vtuber_fanhub.dto.responses.MemberWithReportsResponse;

import java.util.List;

public interface ReportMemberService {

    String createReportMember(CreateReportMemberRequest createReportMemberRequest);

    List<ReportMemberResponse> getReportMembersByFanHubId(Long fanHubId, int pageNo, int pageSize, String sortBy);

    String resolveReportMember(Long reportId, String resolveMessage);

    List<ReportMemberResponse> getReportMembersByCurrentUser(int pageNo, int pageSize, String sortBy);

    List<ReportMemberResponse> getPendingReportMembersByFanHubId(Long fanHubId, int pageNo, int pageSize, String sortBy);

    String bulkResolveReportMembers(List<Long> reportIds, String resolveMessage);

    List<MemberWithReportsResponse> getAllMembersWithReports(Long fanHubId, int pageNo, int pageSize, String sortBy);
}
