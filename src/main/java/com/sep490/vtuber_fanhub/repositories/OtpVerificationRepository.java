package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    OtpVerification findTopByEmailOrderByExpiresAtDesc(String email);

    @Query("SELECT COUNT(o) FROM OtpVerification o WHERE o.email = :email AND o.createdAt > :oneHourAgo")
    long countByEmailAndCreatedAtAfter(@Param("email") String email, @Param("oneHourAgo") java.time.Instant oneHourAgo);
}