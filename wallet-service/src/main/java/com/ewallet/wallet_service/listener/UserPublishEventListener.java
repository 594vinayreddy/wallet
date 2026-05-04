package com.ewallet.wallet_service.listener;

import com.ewallet.wallet_service.config.RabbitMQConfig;
import com.ewallet.wallet_service.event.UserPublishEvent;
import com.ewallet.wallet_service.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserPublishEventListener {

    private final WalletRepository walletRepository;

    @RabbitListener(queues = RabbitMQConfig.USER_TO_WALLET_QUEUE)
    @Transactional
    public void onUserPublished(UserPublishEvent event) {
        log.info("Received user event for userId: {}", event.getUserId());

      if (!walletRepository.existsByUserId(event.getUserId())) {
            log.warn("There is no wallet for email: {}", event.getEmail());
            return;
        }

        walletRepository.deleteByUserId(event.getUserId());
        log.info("Deleted wallet for userId: {}", event.getUserId());
    }
}
