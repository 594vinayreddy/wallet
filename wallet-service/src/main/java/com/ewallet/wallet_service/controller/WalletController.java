package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.dto.SetPinRequest;
import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;


@RestController("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;


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
    public ResponseEntity<Wallet> credit(@PathVariable Long userId,
                                         @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(walletService.credit(userId, amount));
    }

    @PostMapping("/{userId}/debit")
    public ResponseEntity<Wallet> debit(@PathVariable Long userId,
                                        @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(walletService.debit(userId, amount));
    }

}
