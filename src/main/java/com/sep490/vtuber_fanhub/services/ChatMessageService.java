package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.SendMessageRequest;
import com.sep490.vtuber_fanhub.dto.responses.MessageResponse;
import com.sep490.vtuber_fanhub.dto.responses.PaginatedResponse;

public interface ChatMessageService {
    MessageResponse sendMessage(SendMessageRequest sendMessageRequest, String username);
    PaginatedResponse<MessageResponse> getMessagesPaginated(String username, int page, int size);
}
