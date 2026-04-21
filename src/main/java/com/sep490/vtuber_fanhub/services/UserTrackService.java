package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.models.User;

public interface UserTrackService {
    void updateOnLike(User user);

    void updateOnComment(User user);
}
