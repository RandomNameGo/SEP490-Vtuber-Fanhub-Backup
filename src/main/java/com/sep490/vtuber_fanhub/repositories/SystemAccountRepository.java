package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.SystemAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemAccountRepository extends JpaRepository<SystemAccount, Long> {

    Optional<SystemAccount> findByUsername(String username);

    Boolean existsByUsername(String username);
}