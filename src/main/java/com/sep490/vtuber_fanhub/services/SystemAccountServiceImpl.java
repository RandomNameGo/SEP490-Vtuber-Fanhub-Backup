package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateSystemAccountRequest;
import com.sep490.vtuber_fanhub.models.SystemAccount;
import com.sep490.vtuber_fanhub.repositories.SystemAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SystemAccountServiceImpl implements SystemAccountService {

    private final SystemAccountRepository systemAccountRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public String createSystemAccount(CreateSystemAccountRequest request) {

        if(systemAccountRepository.existsByUsername(request.getUsername())) {
            return "Username is already in use";
        }

        SystemAccount systemAccount = new SystemAccount();
        systemAccount.setUsername(request.getUsername());
        systemAccount.setRole(request.getRole());
        systemAccount.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        systemAccountRepository.save(systemAccount);

        return "Created system account successfully";
    }
}
