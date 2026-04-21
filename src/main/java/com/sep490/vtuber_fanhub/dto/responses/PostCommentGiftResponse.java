package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;

@Data
public class PostCommentGiftResponse {

    private Long giftId;

    private Long senderId;
    private String senderUsername;
    private String senderDisplayName;

    private Long receiverId;
    private String receiverUsername;
    private String receiverDisplayName;

    private Long amount;

    private Instant receiveAt;
}
