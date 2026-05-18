package com.ewallet.wallet_service.dto;

import lombok.Data;

@Data
public class ChangePinRequest {
    private Long userId;
    private String oldPin;
    private String newPin;
}