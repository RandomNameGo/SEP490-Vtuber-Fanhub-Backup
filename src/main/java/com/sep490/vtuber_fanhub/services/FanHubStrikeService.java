package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateFanHubStrikeRequest;
import com.sep490.vtuber_fanhub.dto.responses.FanHubStrikeResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface FanHubStrikeService {
    FanHubStrikeResponse createStrike(CreateFanHubStrikeRequest request, HttpServletRequest httpServletRequest);
}
