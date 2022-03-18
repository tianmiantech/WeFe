package com.welab.wefe.board.service.service;


import com.welab.wefe.board.service.database.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class EncryptPhoneNumberService {
    @Autowired
    private AccountRepository accountRepository;
}
