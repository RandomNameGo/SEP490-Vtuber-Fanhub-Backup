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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {

    String createUser(CreateUserRequest createUserRequest);

    String updateUser(UpdateUserRequest updateUserRequest);

    String updateEmail(UpdateEmailRequest updateEmailRequest);

    String changePassword(ChangePasswordRequest changePasswordRequest);

    String uploadAvatarFrame(MultipartFile avatarFile, String frameUrl) throws IOException;

    UserResponse getUserDetailWithBadge(Long userId);

    UserResponse getUserDetailWithBadgeByUserName(String userName);

    List<UserResponse.UserAllBadgeResponse> getAllUserBadges(Long userId);

    String updateUserBadgeDisplay(SelectUserBadgeRequest request);

    String setOshi(SetOshiRequest request);

    UserDetailResponse getCurrentUserDetail();

    UserDailyMissionResponse getMyDailyMission();
}
