package com.ewallet.wallet_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPublishEvent {
    private Long userId;
    private String email;
}
