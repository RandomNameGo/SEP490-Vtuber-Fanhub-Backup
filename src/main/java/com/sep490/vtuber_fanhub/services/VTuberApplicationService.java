package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateVTuberApplication;
import com.sep490.vtuber_fanhub.dto.responses.VTuberApplicationResponse;

import java.util.List;

public interface VTuberApplicationService {
    String createVTuberApplication(CreateVTuberApplication request);

    List<VTuberApplicationResponse> getAllVTuberApplications(int pageNo, int pageSize, String sortBy);

    List<VTuberApplicationResponse> getMyVTuberApplications();

    String reviewVTuberApplication (long vTuberApplicationId, String status, String reason);
}
