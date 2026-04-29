package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.config.RabbitMQConfig;
import com.ewallet.wallet_service.dto.SetPinRequest;
import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.event.NotificationEvent;
import com.ewallet.wallet_service.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private  final WalletRepository walletRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;

    public void setPin(SetPinRequest request, String number) {
        Wallet wallet = walletRepository.findByEmail(request.getEmail());
        if (wallet == null) {
            throw new RuntimeException("Wallet not found for email: " + request.getEmail());
        }

        String hashedPin = passwordEncoder.encode(request.getPin());
        wallet.setPin(hashedPin);

        walletRepository.save(wallet);
        log.info("PIN set successfully for email: {}", request.getEmail());
    }

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
        Wallet savedWallet=walletRepository.save(wallet);

        NotificationEvent notificationEvent= new NotificationEvent(NotificationEvent.Type.RECEIVED,wallet.getEmail(),amount);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_MONEY_RECEIVED,
                notificationEvent
        );
        log.info("Published to notification service for email: {}", notificationEvent.getEmail());

        return savedWallet;
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
        Wallet savedWallet=walletRepository.save(wallet);

        NotificationEvent notificationEvent= new NotificationEvent(NotificationEvent.Type.SENT,wallet.getEmail(),amount);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_MONEY_SENT,
                notificationEvent
        );
        log.info("Published to notification service for email: {}", notificationEvent.getEmail());

        return savedWallet;
    }
}
