package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.dto.*;
import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.repository.WalletRepository;
import com.ewallet.wallet_service.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;


@RestController
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WalletRepository walletRepository;

    @GetMapping("/{userId}/history")
    public ResponseEntity<List<WalletTransactionDTO>> getHistory(@PathVariable Long userId) {
        List<WalletTransactionDTO> history = walletService.getHistory(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Wallet> getWallet(@PathVariable Long userId){
        return ResponseEntity.ok(walletService.getWalletByUserId(userId));
    }

    @PostMapping("/set-pin")
    public ResponseEntity<?> setPin(@RequestBody SetPinRequest request) {
        walletService.setPin(request);
        return ResponseEntity.ok("PIN set successfully");
    }

    @PostMapping("/update-pin")
    public ResponseEntity<?> updatePin(@RequestBody ChangePinRequest request) {
        walletService.changePin(request);
        return ResponseEntity.ok("PIN updated successfully");
    }


    @PostMapping("/{userId}/credit")
    public ResponseEntity<Void> credit(@PathVariable Long userId,
                                         @RequestParam BigDecimal amount) {
        if (!walletRepository.existsByUserId(userId)) {
            return ResponseEntity.notFound().build();
        }
        walletService.credit(userId, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/debit")
    public ResponseEntity<Void> debit(@PathVariable Long userId,
                                        @RequestParam BigDecimal amount, String pin) {
        if (!walletRepository.existsByUserId(userId)) {
            return ResponseEntity.notFound().build();
        }
        walletService.debit(userId, amount,pin);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{email}/creditOnTransaction")
    public ResponseEntity<Void> creditOnTransaction(@RequestBody CreditRequest request) {
        walletService.creditOnTransaction(request.getEmail(), request.getAmount());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{email}/debitOnTransaction")
    public ResponseEntity<Void> debitOnTransaction(@RequestBody DebitRequest request) {
        walletService.debitOnTransaction(request.getEmail(), request.getAmount(), request.getPin());
        return ResponseEntity.ok().build();
    }


    @GetMapping("/{userId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long userId) {
        Wallet wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(wallet.getBalance());
    }

}
