package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.config.RabbitMQConfig;
import com.ewallet.wallet_service.dto.ChangePinRequest;
import com.ewallet.wallet_service.dto.SetPinRequest;
import com.ewallet.wallet_service.dto.WalletTransactionDTO;
import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.entity.WalletTransaction;
import com.ewallet.wallet_service.event.NotificationEvent;
import com.ewallet.wallet_service.repository.WalletRepository;
import com.ewallet.wallet_service.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;

    public void setPin(SetPinRequest request) {

        Wallet wallet = getWalletByUserId(request.getUserId());

        if (wallet.getPin() != null && !wallet.getPin().isEmpty()) {
            throw new RuntimeException("PIN already set. Use change-pin to update it.");
        }
        wallet.setPin(passwordEncoder.encode(request.getPin()));
        walletRepository.save(wallet);
        log.info("PIN set successfully for userId: {}", request.getUserId());
    }

    public void changePin(ChangePinRequest request) {
        Wallet wallet = getWalletByUserId(request.getUserId());
        if (wallet.getPin() == null || wallet.getPin().isEmpty()) {
            throw new RuntimeException("No PIN set. Please set a PIN first.");
        }
        if (!passwordEncoder.matches(request.getOldPin(), wallet.getPin())) {
            throw new RuntimeException("Current PIN is incorrect.");
        }
        wallet.setPin(passwordEncoder.encode(request.getNewPin()));
        walletRepository.save(wallet);
        log.info("PIN changed successfully for userId: {}", request.getUserId());
    }

    public boolean verifyPin(Long userId, String pin) {
        Wallet wallet = getWalletByUserId(userId);

        if (wallet.getPin() == null || wallet.getPin().isEmpty()) {
            throw new RuntimeException("No PIN set for this wallet.");
        }

        return passwordEncoder.matches(pin, wallet.getPin());
    }

    public Wallet getWalletByUserId(Long userId) {

        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for userId: " + userId));
    }

    @Transactional
    public void credit(Long userId, BigDecimal amount) {
        validateAmount(amount);

        Wallet wallet = getWalletByUserId(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        logTransaction(wallet.getId(), WalletTransaction.TransactionType.CREDIT, amount);

        publishNotification(
                NotificationEvent.Type.RECEIVED,
                wallet.getEmail(),
                amount,
                RabbitMQConfig.ROUTING_KEY_MONEY_RECEIVED
        );

        log.info("Amount credited successfully to userId: {}", userId);
    }

    @Transactional
    public void creditOnTransaction(String email, BigDecimal amount) {
        validateAmount(amount);
        Wallet wallet = walletRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Wallet not found for email: " + email));

        wallet.setBalance(wallet.getBalance().add(amount));

        walletRepository.save(wallet);

        publishNotification(
                NotificationEvent.Type.RECEIVED,
                wallet.getEmail(),
                amount,
                RabbitMQConfig.ROUTING_KEY_MONEY_RECEIVED
        );

        log.info("Amount credited successfully to email: {}", email);
    }

    @Transactional
    public void debit(Long userId, BigDecimal amount, String pin) {

        validateAmount(amount);
        Wallet wallet = getWalletByUserId(userId);

        validatePin(wallet, pin);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance.");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));

        walletRepository.save(wallet);

        logTransaction(wallet.getId(), WalletTransaction.TransactionType.DEBIT, amount);

        publishNotification(
                NotificationEvent.Type.SENT,
                wallet.getEmail(),
                amount,
                RabbitMQConfig.ROUTING_KEY_MONEY_SENT
        );

        log.info("Amount debited successfully from userId: {}", userId);
    }

    @Transactional
    public void debitOnTransaction(String email, BigDecimal amount, String pin) {
        validateAmount(amount);
        Wallet wallet = walletRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Wallet not found for email: " + email));

        validatePin(wallet, pin);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException(
                    "Insufficient balance."
            );
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));

        walletRepository.save(wallet);

        publishNotification(
                NotificationEvent.Type.SENT,
                wallet.getEmail(),
                amount,
                RabbitMQConfig.ROUTING_KEY_MONEY_SENT
        );

        log.info("Amount debited successfully from email: {}", email);
    }

    public List<WalletTransactionDTO> getHistory(Long walletId) {

        if (!walletRepository.existsById(walletId)) {
            throw new RuntimeException("Wallet not found.");
        }

        return transactionRepository
                .findByWalletIdOrderByCreatedAtDesc(walletId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
    }

    private void validatePin(Wallet wallet, String pin) {

        if (wallet.getPin() == null || wallet.getPin().isEmpty()) {
            throw new RuntimeException("No PIN set for this wallet.");
        }

        if (!passwordEncoder.matches(pin, wallet.getPin())) {
            throw new RuntimeException("Invalid PIN.");
        }
    }

    private void logTransaction(Long walletId, WalletTransaction.TransactionType type, BigDecimal amount) {
        WalletTransaction tx = new WalletTransaction();
        tx.setWalletId(walletId);
        tx.setType(type);
        tx.setAmount(amount);
        transactionRepository.save(tx);
    }

    private void publishNotification(NotificationEvent.Type type, String email, BigDecimal amount, String routingKey) {
        NotificationEvent event = new NotificationEvent(type, email, amount);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                routingKey,
                event
        );

        log.info("Published {} notification for email: {}", type, email);
    }

    private WalletTransactionDTO toDTO(WalletTransaction tx) {

        WalletTransactionDTO dto = new WalletTransactionDTO();

        dto.setId(tx.getId());
        dto.setWalletId(tx.getWalletId());
        dto.setType(tx.getType());
        dto.setAmount(tx.getAmount());
        dto.setCreatedAt(tx.getCreatedAt());

        return dto;
    }
}