package com.ewallet.wallet_service.dto;

import com.ewallet.wallet_service.entity.WalletTransaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletTransactionDTO {

    private Long id;
    private Long walletId;
    private WalletTransaction.TransactionType type;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
