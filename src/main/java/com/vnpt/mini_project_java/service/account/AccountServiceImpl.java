package com.vnpt.mini_project_java.service.account;

import com.vnpt.mini_project_java.dto.AccountDTO;
import com.vnpt.mini_project_java.dto.LoginDTO;
import com.vnpt.mini_project_java.entity.Account;
import com.vnpt.mini_project_java.response.LoginMesage;
import com.vnpt.mini_project_java.respository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

	@Autowired
	private final AccountRepository accountRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private static final String DATE_FORMAT = "yyyy-MM-dd";

	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

	public AccountServiceImpl(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public Account save(Account account) {
		return accountRepository.save(account);
	}

	@Override
	public List<Account> findAll() {
		return accountRepository.findAll();
	}

	@Override
	@Query(value = "SELECT * FROM account  WHERE account_phone = ?", nativeQuery = true)
	public Optional<Account> findByphone(String phone) {
		return accountRepository.findByphone(phone);
	}

	@Override
	public Account findByAccountName(String accountName) {
		return accountRepository.findByAccountName(accountName);
	}

	@Override
	public Optional<Account> findByname(String accountName) {
		return accountRepository.findByname(accountName);
	}

	@Override
	public List<AccountDTO> getAllAccountDTO() {
		List<Account> accounts = accountRepository.findAll();
		return accounts.stream().map(AccountDTO::new).collect(Collectors.toList());
	}

	@Override
	public Optional<Account> findById(Long accountID) {
		return accountRepository.findById(accountID);
	}

	@Override
	public Account getAccountById(long accountID) {
		Optional<Account> result = accountRepository.findById(accountID);
		if (result.isPresent()) {
			return result.get();
		} else {
			throw new RuntimeException("Product not found with ID: " + accountID);
		}
	}

	@Override
	public String addAccount(AccountDTO accountDTO, MultipartFile image) {

		if (accountRepository.findByAccountName(accountDTO.getAccountName()) != null) {
			throw new RuntimeException("Tài khoản đã tồn tại");
		}

		if (accountRepository.findByEmail(accountDTO.getEmail()) != null) {
			throw new RuntimeException("Email đã được sử dụng");
		}

		Account account = new Account();

		account.setAccountID(accountDTO.getAccountID());
		account.setAccountName(accountDTO.getAccountName());
		account.setAccountPass(this.passwordEncoder.encode(accountDTO.getAccountPass()));
		account.setDateOfBirth(LocalDate.parse(accountDTO.getDateOfBirth(), dateTimeFormatter));
		account.setEmail(accountDTO.getEmail());
		account.setUsername(accountDTO.getUsername());
		account.setPhoneNumber(accountDTO.getPhoneNumber());
		account.setLocal(accountDTO.getLocal());

		account.setTypeAccount("USER");

		if (image != null && !image.isEmpty()) {
			String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
			Path uploadDir = Paths.get("src/main/resources/static/images");

			if (!Files.exists(uploadDir)) {
				try {
					Files.createDirectories(uploadDir);
				} catch (IOException ex) {
					throw new RuntimeException("Không thể tạo thư mục upload", ex);
				}
			}
			Path filePath = uploadDir.resolve(fileName);
			try {
				image.transferTo(filePath);
			} catch (IOException ex) {
				throw new RuntimeException("Không thể lưu file hình ảnh", ex);
			}
			String imagePath = fileName;
			account.setImage(imagePath);
		}
		accountRepository.save(account);
		return account.getAccountName();
	}

	@Override
	public void updateAccount(long accountID,AccountDTO accountDTO, MultipartFile image) {
		Account account = accountRepository.findById(accountDTO.getAccountID())
				.orElseThrow(() ->
						new RuntimeException("Không tìm thấy tài khoản với ID: " + accountDTO.getAccountID()));

		account.setAccountName(accountDTO.getAccountName());

		if(accountDTO.getAccountPass() != null && !accountDTO.getAccountPass().trim().isEmpty()) {
			account.setAccountPass(passwordEncoder.encode(accountDTO.getAccountPass()));
		}

		account.setDateOfBirth(LocalDate.parse(accountDTO.getDateOfBirth(), DateTimeFormatter.ISO_DATE));
		account.setEmail(accountDTO.getEmail());
		account.setUsername(accountDTO.getUsername());
		account.setPhoneNumber(accountDTO.getPhoneNumber());
		account.setLocal(accountDTO.getLocal());

		if (image != null && !image.isEmpty()) {
			String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
			Path uploadDir = Paths.get("src/main/resources/static/images");
			try {
				if (!Files.exists(uploadDir)) {
					Files.createDirectories(uploadDir);
				}
				Path filePath = uploadDir.resolve(fileName);
				image.transferTo(filePath);
				account.setImage(fileName);
			} catch (IOException e) {
				throw new RuntimeException("Không thể lưu hình ảnh", e);
			}
		}
		accountRepository.save(account);
	}

	@Override
	public LoginMesage loginAccount(LoginDTO loginDTO, HttpSession session) {
		String sessionCaptcha = (String) session.getAttribute("captcha");
		if (sessionCaptcha == null || !sessionCaptcha.equals(loginDTO.getCaptcha())) {
			return new LoginMesage("Captcha không hợp lệ. Vui lòng thử lại.",
					false, false, false, false, false, null);
		}

		Account account = accountRepository.findByAccountName(loginDTO.getAccountName());
		if (account == null) {
			return new LoginMesage("Email hoặc tài khoản đăng nhập không chính xác",
					false, false, false, false, false, null);
		}

		String rawPassword = loginDTO.getAccountPass();
		String encodedPassword = account.getAccountPass();
		boolean isPwdRight = passwordEncoder.matches(rawPassword, encodedPassword);

		if (!isPwdRight) {
			return new LoginMesage("Mật khẩu không chính xác. Vui lòng thử lại!",
					false, false, false, false, false, null);
		}

		String typeAccount = account.getTypeAccount();
		boolean isAdmin = typeAccount.equals(Account.ADMIN);
		boolean isUser = typeAccount.equals(Account.USER);
		boolean isEmployee = typeAccount.equals(Account.EMPLOYEE);

		if (isAdmin) {
			return new LoginMesage("Login Success", true,
					true, false, false, true, "ADMIN");
		} else if (isEmployee) {
			return new LoginMesage("Login Success", true,
					false, false, true, true, "EMPLOYEE");
		} else if (isUser) {
			return new LoginMesage("Login Success", true,
					false, true, false, true, "USER");
		} else {
			return new LoginMesage("Login Success", true,
					false, false, false, true, "GUEST");
		}
	}

	@Override
	public void changePassword(Long accountID, String oldPassword, String newPassword) {
		Account account = accountRepository.findById(accountID)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

		if (!passwordEncoder.matches(oldPassword, account.getAccountPass())) {
			throw new RuntimeException("Mật khẩu cũ không đúng");
		}

		account.setAccountPass(passwordEncoder.encode(newPassword));
		accountRepository.save(account);
	}
}