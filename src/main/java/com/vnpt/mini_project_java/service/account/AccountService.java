package com.vnpt.mini_project_java.service.account;

import com.vnpt.mini_project_java.dto.AccountDTO;
import com.vnpt.mini_project_java.dto.LoginDTO;
import com.vnpt.mini_project_java.entity.Account;
import com.vnpt.mini_project_java.response.LoginMesage;

import org.springframework.data.jpa.repository.Query;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

public interface AccountService {
    Account save(Account account);

    List<Account> findAll();

    @Query(value = "SELECT * FROM account  WHERE account_phone = ?", nativeQuery = true)
    Optional<Account> findByphone(String phone);

    Account findByAccountName(String accountName);

    Optional<Account> findByname(String accountName);

    List<AccountDTO> getAllAccountDTO();

    Optional<Account> findById(Long accountID);

    Account getAccountById(long accountID);

    String addAccount(AccountDTO accountDTO);

    void updateAccount(long accountID, AccountDTO accountDTO);

    LoginMesage loginAccount(LoginDTO loginDTO, HttpSession session);

    void changePassword(Long accountID, String oldPassword, String newPassword);
}
