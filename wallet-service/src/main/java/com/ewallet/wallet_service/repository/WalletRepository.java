package com.ewallet.wallet_service.repository;

import com.ewallet.wallet_service.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    void deleteByUserId(Long userId);

    boolean existsByUserId(Long userId);

    boolean existsByEmail(String email);

    Optional<Wallet> findByEmail(String email);

    Optional<Wallet> findByUserId(Long userId);
}
