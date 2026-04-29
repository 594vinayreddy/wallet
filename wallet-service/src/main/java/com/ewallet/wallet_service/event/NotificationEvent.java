package com.ewallet.wallet_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {
    public Type type;
    public String email;
    public BigDecimal amount;

    public enum Type {
        SENT,
        RECEIVED
    }
}
