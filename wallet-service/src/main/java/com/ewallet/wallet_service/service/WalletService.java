package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {

    private  final WalletRepository walletRepository;

    public Wallet getWalletByUserId(Long userId){
        return walletRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("Wallet not found for userId: "+userId));
    }

    @Transactional
    public Wallet credit(Long userId, BigDecimal amount){
        if(amount.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("Amount must be positive");
        }
        Wallet wallet=getWalletByUserId(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        return walletRepository.save(wallet);
    }

    @Transactional
    public Wallet debit(Long userId, BigDecimal amount){
        if(amount.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("Amount must be positive");
        }
        Wallet wallet=getWalletByUserId(userId);
        if(wallet.getBalance().compareTo(amount)<0){
            throw new RuntimeException("Insufficient balance");
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        return walletRepository.save(wallet);
    }
}
