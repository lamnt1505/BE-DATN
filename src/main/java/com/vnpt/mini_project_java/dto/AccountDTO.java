package com.vnpt.mini_project_java.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.vnpt.mini_project_java.entity.Account;
import lombok.*;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
	
    private Long accountID;

    private String accountName;

    private String accountPass;

    private String username;

    private String phoneNumber;

    private String dateOfBirth;

    private String image;

    private String local;

    private String email;

    private boolean isAdmin;

    public AccountDTO(Long accountID, String accountName, Object o, String username, String phoneNumber,
                      LocalDate dateOfBirth, String image, String local, String email) {
    }

    public AccountDTO(Account account) {
        this.accountID = account.getAccountID();
        this.accountName = account.getAccountName();
        this.accountPass = account.getAccountPass();
        this.dateOfBirth = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(account.getDateOfBirth());
        this.username = account.getUsername();
        this.email = account.getEmail();
        this.phoneNumber = account.getPhoneNumber();
        this.local = account.getLocal();
        this.isAdmin = account.isAdmin();
        this.image = account.getImage();
    }
}
