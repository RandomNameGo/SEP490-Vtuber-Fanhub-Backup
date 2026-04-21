package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateBanMemberRequest;
import com.sep490.vtuber_fanhub.dto.responses.BanMemberResponse;
import com.sep490.vtuber_fanhub.dto.responses.MemberWithBansResponse;

import java.util.List;

public interface BanMemberService {

    String banFanHubMember(CreateBanMemberRequest request);

    void checkBanStatus(Long hubId, Long userId, List<String> banTypes);

    List<BanMemberResponse> getActiveBansByHubId(Long fanHubId, int pageNo, int pageSize, String sortBy);

    String revokeBan(Long banId);

    void deactivateExpiredBans();

    List<MemberWithBansResponse> getAllMembersWithBans(Long fanHubId, int pageNo, int pageSize, String sortBy);
}
