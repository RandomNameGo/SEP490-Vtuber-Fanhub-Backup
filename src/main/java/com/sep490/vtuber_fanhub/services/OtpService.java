package com.sep490.vtuber_fanhub.services;


import com.sep490.vtuber_fanhub.exceptions.RateLimitException;
import com.sep490.vtuber_fanhub.models.OtpVerification;
import com.sep490.vtuber_fanhub.repositories.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpVerificationRepository otpVerificationRepository;

    private static final int MAX_OTP_PER_HOUR = 10;

    public String generateOtp(String email) {
        // Check rate limit: max 10 OTPs per hour per email
        long otpCountLastHour = otpVerificationRepository.countByEmailAndCreatedAtAfter(
                email, Instant.now().minus(1, ChronoUnit.HOURS));

        if (otpCountLastHour >= MAX_OTP_PER_HOUR) {
            throw new RateLimitException("Too many OTP requests. Please try again after 1 hour.");
        }

        String otp = String.valueOf(new Random().nextInt(999999));
        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(email);
        otpVerification.setOtpCode(otp);
        otpVerification.setExpiresAt((Instant.now().plus(5, ChronoUnit.MINUTES)));
        otpVerification.setCreatedAt(Instant.now());
        otpVerificationRepository.save(otpVerification);
        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        OtpVerification record = otpVerificationRepository.findTopByEmailOrderByExpiresAtDesc(email);

        if (record != null
                && record.getOtpCode().equals(otp)
                && Instant.now().isBefore(record.getExpiresAt())) {
            record.setIsUsed(true);
            return true;
        }

        return false;
    }
}
