package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateSystemAccountRequest;
import com.sep490.vtuber_fanhub.models.SystemAccount;

public interface SystemAccountService {
    String createSystemAccount(CreateSystemAccountRequest request);
}
