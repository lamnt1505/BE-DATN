package com.vnpt.mini_project_java.restcontroller;

import com.vnpt.mini_project_java.dto.AccountDTO;
import com.vnpt.mini_project_java.dto.LoginDTO;
import com.vnpt.mini_project_java.entity.Account;
import com.vnpt.mini_project_java.response.LoginMesage;
import com.vnpt.mini_project_java.service.account.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping(path = "/api/v1/account", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AccountController {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(AccountController.class);
    private static final Long MAIN_ADMIN_ID = 1L;

    @Autowired
    private AccountService accountService;

    private String generateRandomText(int length) {
        String characters = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        StringBuilder captchaText = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            captchaText.append(characters.charAt(index));
        }
        return captchaText.toString();
    }

    @GetMapping("/Listgetall")
    public ResponseEntity<List<AccountDTO>> getList() {
        List<AccountDTO> accountDTOS = accountService.getAllAccountDTO();
        return ResponseEntity.ok(accountDTOS);
    }

    @PostMapping(path = "/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> addAccount(@RequestBody AccountDTO accountDTO) {
        try {
            String accountName = accountService.addAccount(accountDTO);
            return ResponseEntity.ok(Collections.singletonMap(
                    "message", "Tài khoản đã đăng ký thành công với ID: " + accountName
            ));
        } catch (RuntimeException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", ex.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    @PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>>updateAccount( @PathVariable("id") Long accountID, @RequestBody AccountDTO accountDTO) {
        try {
            accountDTO.setAccountID(accountID);
            accountService.updateAccount(accountID, accountDTO);
            return ResponseEntity.ok(Collections.singletonMap("message", "Cập nhật tài khoản thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Có lỗi xảy ra khi cập nhật tài khoản ❌"));
        }
    }

    @GetMapping("/{id}/get")
    public ResponseEntity<AccountDTO> getProductById(@PathVariable(name = "id") Long accountID) {
        Account account = accountService.getAccountById(accountID);
        AccountDTO accountResponse = new AccountDTO(account);
        return ResponseEntity.ok().body(accountResponse);
    }

    @GetMapping("/captcha")
    public void generateCaptcha(HttpServletResponse response, HttpSession session) throws IOException {
        int width = 150, height = 50;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        String captchaText = generateRandomText(5);
        session.setAttribute("captcha", captchaText);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.setColor(Color.BLUE);

        g.drawString(captchaText, 20, 35);
        g.dispose();

        response.setContentType("image/png");
        ImageIO.write(image, "png", response.getOutputStream());
    }

    @PutMapping("/grant-role/{accountID}")
    public ResponseEntity<?> grantRole(HttpServletRequest request, @PathVariable("accountID") Long accountID,
            @RequestParam("role") String requestRole) {

        String headerRole = request.getHeader("X-Role");

        String adminName = request.getHeader("X-Admin-Name");

        Logger logger = LoggerFactory.getLogger(this.getClass());

        if (!"ADMIN".equals(headerRole)) {
            logger.warn("Truy cập bị từ chối: '{}' không phải ADMIN", adminName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không có quyền");
        }

        Optional<Account> optionalAccount = accountService.findById(accountID);

        if (!optionalAccount.isPresent()) {
            logger.warn("Tài khoản không tìm thấy: accountID='{}'", accountID);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng");
        }

        Account user = optionalAccount.get();
        String oldRole = user.getTypeAccount();
        user.setTypeAccount(requestRole.toUpperCase());

        accountService.save(user);

        logger.info("✅ Phân quyền thành công: Admin='{}' đã thay đổi role của '{}' từ '{}' thành '{}'",
                adminName, user.getAccountName(), oldRole, requestRole.toUpperCase());

        return ResponseEntity.ok("Phân quyền thành công! Người dùng giờ là: " + requestRole.toUpperCase());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginMesage> login(@RequestBody LoginDTO loginDTO,HttpServletResponse response,
                                             HttpSession session) {

        LoginMesage loginResponse = accountService.loginAccount(loginDTO, session);
        HttpHeaders headers = new HttpHeaders();

        if (!loginResponse.isCaptchaValid() || !loginResponse.getStatus()) {
            logger.warn("Đăng nhập thất bại cho tài khoản: {}", loginDTO.getAccountName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(loginResponse);
        }

        Account acc = accountService.findByname(loginDTO.getAccountName()).orElse(null);
        if (acc != null) {
            loginResponse.setAccountID(acc.getAccountID());
        }

        Cookie cookie = new Cookie("accountName", loginDTO.getAccountName());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);

        if (loginResponse.isAdmin()) {
            loginResponse.setRole("ADMIN");
        } else if (loginResponse.isEmployee()) {
            loginResponse.setRole("EMPLOYEE");
        } else if (loginResponse.isUser()) {
            loginResponse.setRole("USER");
        } else {
            loginResponse.setRole("GUEST");
        }

        logger.info("Người dùng [{}] đã đăng nhập thành công lúc {}",
                loginDTO.getAccountName(), java.time.LocalDateTime.now());

        return new ResponseEntity<>(loginResponse, headers, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setValue("");
                cookie.setMaxAge(0);
                cookie.setPath("/");
                cookie.setHttpOnly(true);
                cookie.setSecure(false);
                response.addCookie(cookie);
            }
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Đăng xuất thành công");
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/me")
    public ResponseEntity<AccountDTO> getCurrentAccount(
            @CookieValue(value = "accountName", required = false) String accountName) {

        if (accountName == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return accountService.findByname(accountName)
                .map(acc -> new AccountDTO(
                        acc.getAccountID(),
                        acc.getAccountName(),
                        null,
                        acc.getUsername(),
                        acc.getPhoneNumber(),
                        acc.getDateOfBirth(),
                        acc.getImageBase64(),
                        acc.getLocal(),
                        acc.getEmail()
                ))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PutMapping("/changer-password/{id}")
    public ResponseEntity<String> changePassword(
            @PathVariable("id") Long accountID,
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                return ResponseEntity.badRequest().body("Mật khẩu xác nhận không khớp");
            }
            accountService.changePassword(accountID, oldPassword, newPassword);
            return ResponseEntity.ok("Đổi mật khẩu thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Có lỗi xảy ra khi đổi mật khẩu");
        }
    }

    @DeleteMapping("/{accountID}/delete")
    public ResponseEntity<?> deleteAccount( @PathVariable Long accountID, HttpServletRequest request) {
        String adminName = request.getHeader("X-Admin-Name");
        try {
            if (accountID.equals(MAIN_ADMIN_ID)) {
                logger.error("NGĂN CHẶN: Admin='{}' cố gắng xóa admin chính (ID={})",
                        adminName, accountID);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "❌ Không thể xóa admin chính!");
                response.put("code", "CANNOT_DELETE_MAIN_ADMIN");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            long adminCount = accountService.countByTypeAccount("ADMIN");
            Optional<Account> optionalAccount = accountService.findById(accountID);
            if (!optionalAccount.isPresent()) {
                logger.warn("Xóa tài khoản thất bại: Tài khoản ID='{}' không tồn tại", accountID);
                Map<String, Object> response = new HashMap<String, Object>() {{
                    put("success", false);
                    put("message", "Tài khoản không tồn tại");
                }};
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            Account account = optionalAccount.get();
            if ("ADMIN".equals(account.getTypeAccount()) && adminCount <= 1) {
                logger.error("❌ NGĂN CHẶN: Admin='{}' cố gắng xóa admin duy nhất (ID='{}')",
                        adminName, accountID);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "❌ Không thể xóa admin duy nhất trong hệ thống!");
                response.put("code", "CANNOT_DELETE_ONLY_ADMIN");

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            accountService.deleteById(accountID);
            logger.info("✅ Xóa tài khoản thành công: Admin='{}' đã xóa tài khoản ID='{}' ({})",
                    adminName, accountID, account.getAccountName());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ Xóa tài khoản thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Lỗi khi xóa tài khoản ID='{}': {}", accountID, e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/admin/count")
    public ResponseEntity<?> getAdminCount() {
        try {
            long adminCount = accountService.countByTypeAccount("ADMIN");
            Map<String, Object> response = new HashMap<>();
            response.put("adminCount", adminCount);
            response.put("canDeleteAdmin", adminCount > 1);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
