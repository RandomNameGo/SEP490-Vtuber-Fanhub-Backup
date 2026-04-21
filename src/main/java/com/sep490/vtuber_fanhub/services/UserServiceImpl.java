package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.ChangePasswordRequest;
import com.sep490.vtuber_fanhub.dto.requests.CreateUserRequest;
import com.sep490.vtuber_fanhub.dto.requests.SelectUserBadgeRequest;
import com.sep490.vtuber_fanhub.dto.requests.SetOshiRequest;
import com.sep490.vtuber_fanhub.dto.requests.UpdateEmailRequest;
import com.sep490.vtuber_fanhub.dto.requests.UpdateUserRequest;
import com.sep490.vtuber_fanhub.dto.responses.UserDetailResponse;
import com.sep490.vtuber_fanhub.dto.responses.UserDailyMissionResponse;
import com.sep490.vtuber_fanhub.dto.responses.UserResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.FanHubMember;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.models.UserBadge;
import com.sep490.vtuber_fanhub.models.UserDailyMission;
import com.sep490.vtuber_fanhub.repositories.FanHubMemberRepository;
import com.sep490.vtuber_fanhub.repositories.PostCommentGiftRepository;
import com.sep490.vtuber_fanhub.repositories.UserBadgeRepository;
import com.sep490.vtuber_fanhub.repositories.UserDailyMissionRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final HttpServletRequest httpServletRequest;

    private final CloudinaryService cloudinaryService;

    private final UserDailyMissionRepository userDailyMissionRepository;

    private final AuthService authService;

    private final UserBadgeRepository userBadgeRepository;

    private final FanHubMemberRepository fanHubMemberRepository;

    private final PostCommentGiftRepository postCommentGiftRepository;

    private final UserBadgeService userBadgeService;

    @Override
    @Transactional
    public String createUser(CreateUserRequest createUserRequest) {

        if(userRepository.existsByUsername(createUserRequest.getUsername())){
            return "Username is already in use";
        }

        if(userRepository.existsByEmail(createUserRequest.getEmail())){
            return "Email is already in use";
        }

        User user = new User();
        user.setUsername(createUserRequest.getUsername());
        user.setEmail(createUserRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(createUserRequest.getPassword()));
        user.setDisplayName(createUserRequest.getDisplayName());
        user.setBio(createUserRequest.getBio());

        user.setPoints(0L);
        user.setPaidPoints(0L);
        user.setIsActive(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setRole("USER");

        userRepository.save(user);

        UserDailyMission userDailyMission = new UserDailyMission();
        userDailyMission.setUser(user);
        userDailyMission.setLikeAmount(0);
        userDailyMissionRepository.save(userDailyMission);

        // Award badge ID 6 for successful registration
        userBadgeService.awardBadge(user, 6L);

        return "Created user successfully";
    }

    @Override
    @Transactional
    public String uploadAvatarFrame(MultipartFile avatarFile, String frameUrl) throws IOException {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        if(avatarFile != null && !avatarFile.isEmpty()){
            String avatarUrl = cloudinaryService.uploadFile(avatarFile);
            currentUser.setAvatarUrl(avatarUrl);
        }
        if(frameUrl != null && !frameUrl.isEmpty()){
            currentUser.setFrameUrl(frameUrl);
        }

        userRepository.save(currentUser);

        return "Uploaded successfully";
    }

    @Override
    @Transactional
    public String updateUser(UpdateUserRequest updateUserRequest) {

        User user = authService.getUserFromToken(httpServletRequest);

        if (updateUserRequest.getDisplayName() != null) {
            user.setDisplayName(updateUserRequest.getDisplayName());
        }

        if (updateUserRequest.getTranslateLanguage() != null) {
            user.setTranslateLanguage(updateUserRequest.getTranslateLanguage());
        }

        if (updateUserRequest.getBio() != null) {
            user.setBio(updateUserRequest.getBio());
        }

        user.setUpdatedAt(Instant.now());

        userRepository.save(user);

        return "Updated user successfully";
    }

    @Override
    @Transactional
    public String updateEmail(UpdateEmailRequest updateEmailRequest) {
        User user = authService.getUserFromToken(httpServletRequest);

        if (!passwordEncoder.matches(updateEmailRequest.getPassword(), user.getPasswordHash())) {
            return "Incorrect password";
        }

        if (!user.getEmail().equals(updateEmailRequest.getEmail()) && userRepository.existsByEmail(updateEmailRequest.getEmail())) {
            return "Email is already in use";
        }

        user.setEmail(updateEmailRequest.getEmail());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        return "Updated email successfully";
    }

    @Override
    @Transactional
    public String changePassword(ChangePasswordRequest changePasswordRequest) {
        User user = authService.getUserFromToken(httpServletRequest);

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPasswordHash())) {
            return "Old password is incorrect";
        }

        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
            return "New password and confirm password do not match";
        }

        user.setPasswordHash(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        return "Changed password successfully";
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserDetailWithBadge(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        List<UserBadge> displayBadges = userBadgeRepository.findByUserIdAndIsDisplayTrue(userId);
        List<UserBadge> allBadges = userBadgeRepository.findByUserId(userId);

        return mapToUserResponse(user, displayBadges, allBadges, userId);
    }

    @Override
    public UserResponse getUserDetailWithBadgeByUserName(String userName) {
        Optional<User> user = userRepository.findByUsernameAndIsActive(userName);

        if (user.isEmpty()){
            throw new NotFoundException("User not found with name: " + userName);
        }

        List<UserBadge> displayBadges = userBadgeRepository.findByUserIdAndIsDisplayTrue(user.get().getId());
        List<UserBadge> allBadges = userBadgeRepository.findByUserId(user.get().getId());

        return mapToUserResponse(user.get(), displayBadges, allBadges, user.get().getId());
    }

    private UserResponse mapToUserResponse(User user, List<UserBadge> displayBadges, List<UserBadge> allBadges, Long userId) {
        UserResponse response = new UserResponse();

        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setDisplayName(user.getDisplayName());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setFrameUrl(user.getFrameUrl());
        response.setBio(user.getBio());
        response.setRole(user.getRole());
        response.setPoints(user.getPoints());
        response.setPaidPoints(user.getPaidPoints());
        response.setTranslateLanguage(user.getTranslateLanguage());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setIsActive(user.getIsActive());

        if (user.getOshiUser() != null) {
            UserResponse.OshiResponse oshiResponse = new UserResponse.OshiResponse();
            oshiResponse.setUserId(user.getOshiUser().getId());
            oshiResponse.setUsername(user.getOshiUser().getUsername());
            oshiResponse.setDisplayName(user.getOshiUser().getDisplayName());
            oshiResponse.setAvatarUrl(user.getOshiUser().getAvatarUrl());
            response.setOshi(oshiResponse);
        }

        response.setTotalBadges(userBadgeRepository.countByUserId(userId));
        response.setTotalFanHubs(fanHubMemberRepository.countByUserId(userId));
        response.setTotalReceivedGifts(postCommentGiftRepository.countByReceiverId(userId));

        if (displayBadges != null && !displayBadges.isEmpty()) {
            List<UserResponse.UserDisplayBadgeResponse> displayBadgeResponses = new ArrayList<>();
            for (UserBadge userBadge : displayBadges) {
                UserResponse.UserDisplayBadgeResponse badgeResponse = new UserResponse.UserDisplayBadgeResponse();
                badgeResponse.setUserBadgeId(userBadge.getId());
                badgeResponse.setBadgeId(userBadge.getBadge().getId());
                badgeResponse.setBadgeName(userBadge.getBadge().getBadgeName());
                badgeResponse.setDescription(userBadge.getBadge().getDescription());
                badgeResponse.setIconUrl(userBadge.getBadge().getIconUrl());
                badgeResponse.setRequirement(userBadge.getBadge().getRequirement());
                badgeResponse.setAcquiredAt(userBadge.getAcquiredAt());
                badgeResponse.setIsDisplay(userBadge.getIsDisplay());
                displayBadgeResponses.add(badgeResponse);
            }
            response.setDisplayBadges(displayBadgeResponses);
        }

        if (allBadges != null && !allBadges.isEmpty()) {
            List<UserResponse.UserAllBadgeResponse> allBadgeResponses = new ArrayList<>();
            for (UserBadge userBadge : allBadges) {
                UserResponse.UserAllBadgeResponse badgeResponse = new UserResponse.UserAllBadgeResponse();
                badgeResponse.setUserBadgeId(userBadge.getId());
                badgeResponse.setBadgeId(userBadge.getBadge().getId());
                badgeResponse.setBadgeName(userBadge.getBadge().getBadgeName());
                badgeResponse.setDescription(userBadge.getBadge().getDescription());
                badgeResponse.setIconUrl(userBadge.getBadge().getIconUrl());
                badgeResponse.setRequirement(userBadge.getBadge().getRequirement());
                badgeResponse.setAcquiredAt(userBadge.getAcquiredAt());
                badgeResponse.setIsDisplay(userBadge.getIsDisplay());
                allBadgeResponses.add(badgeResponse);
            }
            response.setAllBadges(allBadgeResponses);
        }

        List<FanHubMember> fanHubMembers = fanHubMemberRepository.findAllByUserId(userId);
        if (fanHubMembers != null && !fanHubMembers.isEmpty()) {
            List<UserResponse.UserHubJoinedResponse> hubJoinedResponses = new ArrayList<>();
            for (FanHubMember member : fanHubMembers) {
                UserResponse.UserHubJoinedResponse hubResponse = new UserResponse.UserHubJoinedResponse();
                hubResponse.setFanHubId(member.getHub().getId());
                hubResponse.setSubdomain(member.getHub().getSubdomain());
                hubResponse.setHubName(member.getHub().getHubName());
                hubResponse.setThemeColor(member.getHub().getThemeColor());
                hubResponse.setAvatarUrl(member.getHub().getAvatarUrl());
                hubJoinedResponses.add(hubResponse);
            }
            response.setFanHubsJoined(hubJoinedResponses);
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse.UserAllBadgeResponse> getAllUserBadges(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        List<UserBadge> userBadges = userBadgeRepository.findByUserId(userId);

        List<UserResponse.UserAllBadgeResponse> badgeResponses = new ArrayList<>();
        for (UserBadge userBadge : userBadges) {
            UserResponse.UserAllBadgeResponse badgeResponse = new UserResponse.UserAllBadgeResponse();
            badgeResponse.setUserBadgeId(userBadge.getId());
            badgeResponse.setBadgeId(userBadge.getBadge().getId());
            badgeResponse.setBadgeName(userBadge.getBadge().getBadgeName());
            badgeResponse.setDescription(userBadge.getBadge().getDescription());
            badgeResponse.setIconUrl(userBadge.getBadge().getIconUrl());
            badgeResponse.setRequirement(userBadge.getBadge().getRequirement());
            badgeResponse.setAcquiredAt(userBadge.getAcquiredAt());
            badgeResponse.setIsDisplay(userBadge.getIsDisplay());
            badgeResponses.add(badgeResponse);
        }

        return badgeResponses;
    }


    @Override
    @Transactional
    public String updateUserBadgeDisplay(SelectUserBadgeRequest request) {
        User user = authService.getUserFromToken(httpServletRequest);

        List<Long> selectedBadgeIds = request.getUserBadgeIds();

        if (selectedBadgeIds == null) {
            selectedBadgeIds = new ArrayList<>();
        }

        if (selectedBadgeIds.size() > 3) {
            return "Maximum 3 badges can be displayed";
        }

        List<UserBadge> allUserBadges = userBadgeRepository.findByUserId(user.getId());

        for (UserBadge userBadge : allUserBadges) {
            if (selectedBadgeIds.contains(userBadge.getId())) {
                userBadge.setIsDisplay(true);
            } else {
                userBadge.setIsDisplay(false);
            }
        }

        userBadgeRepository.saveAll(allUserBadges);

        return "Updated badge display successfully";
    }

    @Override
    @Transactional
    public String setOshi(SetOshiRequest request) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        User oshiUser = userRepository.findByUsernameAndIsActive(request.getOshiUsername())
                .orElseThrow(() -> new NotFoundException("VTuber not found with username: " + request.getOshiUsername()));

        if (!"VTUBER".equals(oshiUser.getRole())) {
            return "User with username '" + request.getOshiUsername() + "' is not a VTUBER";
        }

        currentUser.setOshiUser(oshiUser);
        currentUser.setUpdatedAt(Instant.now());
        userRepository.save(currentUser);

        return "Set oshi successfully";
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getCurrentUserDetail() {
        User user = authService.getUserFromToken(httpServletRequest);

        UserDetailResponse response = new UserDetailResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setDisplayName(user.getDisplayName());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setFrameUrl(user.getFrameUrl());
        response.setBio(user.getBio());
        response.setRole(user.getRole());
        response.setPoints(user.getPoints());
        response.setPaidPoints(user.getPaidPoints());
        response.setTranslateLanguage(user.getTranslateLanguage());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setIsActive(user.getIsActive());

        if (user.getOshiUser() != null) {
            UserDetailResponse.OshiResponse oshiResponse = new UserDetailResponse.OshiResponse();
            oshiResponse.setUserId(user.getOshiUser().getId());
            oshiResponse.setUsername(user.getOshiUser().getUsername());
            oshiResponse.setDisplayName(user.getOshiUser().getDisplayName());
            oshiResponse.setAvatarUrl(user.getOshiUser().getAvatarUrl());
            response.setOshi(oshiResponse);
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDailyMissionResponse getMyDailyMission() {
        User user = authService.getUserFromToken(httpServletRequest);

        UserDailyMission mission = userDailyMissionRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("User daily mission not found"));

        UserDailyMissionResponse response = new UserDailyMissionResponse();
        response.setLikeAmount(mission.getLikeAmount());
        response.setBonus10(mission.getBonus10());
        response.setBonus20(mission.getBonus20());

        return response;
    }
}
