package com.ewallet.wallet_service.dto;

import lombok.Data;

@Data
public class VerifyPinRequest {
    Long userId;
    String pin;
}
