package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.MessageResponse;
import com.sep490.vtuber_fanhub.models.User;

public interface AiResponseService {
    MessageResponse generateAndSendReply(User user, String userMessageContent);

}
