package com.ewallet.wallet_service.listener;

import com.ewallet.wallet_service.config.RabbitMQConfig;
import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.event.UserRegisteredEvent;
import com.ewallet.wallet_service.repository.WalletRepository;
import com.ewallet.wallet_service.service.WalletService;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletRegisteredListener {

    private final WalletRepository walletRepository;
    private final WalletService walletService;

    @RabbitListener(queues = RabbitMQConfig.WALLET_QUEUE)
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("Wallet Service received event for userId: {}", event.getUserId());

        if (walletRepository.existsById(event.getUserId())){
            log.warn("Wallet already exists for userId: {}", event.getUserId());
            return;
        }

        Wallet wallet = Wallet.builder()
                .userId(event.getUserId())
                .email(event.getEmail())
                .balance(BigDecimal.ZERO)
                .pin("1234") // directly set
                .build();

        walletRepository.save(wallet);
        log.info("Created wallet for userId: {}", event.getUserId());

    }
}