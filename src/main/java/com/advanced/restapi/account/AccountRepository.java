package com.advanced.restapi.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByEmail(String username);

    Optional<Account> findByAccountId(String accountId);
}