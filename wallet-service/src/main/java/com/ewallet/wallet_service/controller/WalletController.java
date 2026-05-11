package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.dto.SetPinRequest;
import com.ewallet.wallet_service.dto.WalletTransactionDTO;
import com.ewallet.wallet_service.entity.Wallet;
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
        walletService.setPin(request, "1234");
        return ResponseEntity.ok("PIN set successfully");
    }

    @PostMapping("/{userId}/credit")
    public ResponseEntity<Void> credit(@PathVariable Long userId,
                                         @RequestParam BigDecimal amount) {
        walletService.credit(userId, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/debit")
    public ResponseEntity<Void> debit(@PathVariable Long userId,
                                        @RequestParam BigDecimal amount) {
        walletService.debit(userId, amount);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long userId) {
        Wallet wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(wallet.getBalance());
    }

}
