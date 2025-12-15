package com.vnpt.mini_project_java.restcontroller;

import com.vnpt.mini_project_java.config.VnpayConfig;
import com.vnpt.mini_project_java.dto.OrderRequestDTO;
import com.vnpt.mini_project_java.entity.Account;
import com.vnpt.mini_project_java.entity.Order;
import com.vnpt.mini_project_java.entity.OrderDetail;
import com.vnpt.mini_project_java.entity.Product;
import com.vnpt.mini_project_java.respository.DiscountRepository;
import com.vnpt.mini_project_java.respository.DiscountUsageRepository;
import com.vnpt.mini_project_java.service.account.AccountService;
import com.vnpt.mini_project_java.service.email.EmailService;
import com.vnpt.mini_project_java.service.order.OrderService;
import com.vnpt.mini_project_java.service.orderDetail.OrderDetailService;
import com.vnpt.mini_project_java.service.productvotes.ProductVotesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class IntegrationVnpayController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    ProductVotesService productVotesService;

    @Autowired
    DiscountRepository discountRepository;

    @Autowired
    DiscountUsageRepository discountUsageRepository;

    private void logToConsoleAndFile(String message) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info(message);
    }

    @PostMapping("/orders/vnpay")
    @ResponseBody
    public ResponseEntity<?> createOrderVnpay(@RequestBody OrderRequestDTO request, HttpServletRequest httpRequest,
                                              HttpSession session) {
        Logger logger = LoggerFactory.getLogger(this.getClass());

        Account account = null;

        String accountIdHeader = httpRequest.getHeader("X-Account-ID");
        if (accountIdHeader != null && !accountIdHeader.isEmpty()) {
            try {
                Long accountID = Long.parseLong(accountIdHeader);
                account = this.accountService.findById(accountID).orElse(null);
                logger.info("Found account from header: {}", accountID);
            } catch (Exception e) {
                logger.error("Error parsing accountID from header: {}", e.getMessage());
            }
        }

        if (account == null) {
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accountName".equals(cookie.getName())) {
                        account = this.accountService.findByname(cookie.getValue()).orElse(null);
                        break;
                    }
                }
            }
        }

        if (account == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "fail");
            response.put("message", "Bạn cần đăng nhập");
            return ResponseEntity.ok(response);
        }

        List<Product> cart = (List<Product>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "fail");
            response.put("message", "Giỏ hàng trống");
            return ResponseEntity.ok(response);
        }

        Double total = (Double) session.getAttribute("discountedTotal");
        if (total == null) {
            total = cart.stream().mapToDouble(p -> p.getPrice() * p.getAmount()).sum();
        }

        logger.info("Tổng tiền (sau giảm giá): {}", total);

        String txnRef = String.valueOf(System.currentTimeMillis()) +
                "_" +
                UUID.randomUUID().toString().substring(0, 8);//sinh ra mã giao dịch

        Order order = new Order();
        long millis = System.currentTimeMillis();
        java.sql.Date date = new java.sql.Date(millis);
        LocalDate localDate = date.toLocalDate();

        order.setAccount(account);
        order.setOrderDateImport(localDate);
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setShippingAddress(request.getShippingAddress());
        order.setNote(request.getNote());

        order.setStatus("CHỜ THANH TOÁN");
        order.setPaymentMethod("VNPAY");
        order.setOrderTotal(total);
        order.setTxnRef(txnRef);

        orderService.save(order);// lưu đơn han

        Set<OrderDetail> setDetail = new HashSet<>();
        for (Product product : cart) {
            OrderDetail s = new OrderDetail();
            s.setProduct(product);
            s.setAmount(product.getAmount());
            s.setPrice(product.getPrice());
            s.setOrder(order);
            setDetail.add(s);
            orderDetailService.save(s);//lưu thông tin chi tiết đơn hàng
        }
        order.setOrderDetails(setDetail);

        session.setAttribute("cart", new ArrayList<>());
        session.removeAttribute("cart");
        session.removeAttribute("discountedTotal");
        session.setAttribute("currentTxnRef", txnRef);

        Map<String, Object> res = new HashMap<>();
        res.put("status", "success");
        res.put("orderId", order.getOrderID());
        res.put("orderNumber", order.getOrderNumber());
        res.put("txnRef", txnRef);
        res.put("total", total);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/create-payment")// tạo link thanh toán
    @ResponseBody
    public ResponseEntity<?> createPayment(@RequestParam("txnRef") String txnRef,
                                           HttpServletRequest request) throws UnsupportedEncodingException {
        Order order = orderService.findByTxnRef(txnRef);
        if (order == null) {
            Map<String, Object> res = new HashMap<>();
            res.put("status", "fail");
            res.put("message", "Không tìm thấy đơn hàng với txnRef: " + txnRef);
            return ResponseEntity.ok(res);
        }

        if (!"CHỜ THANH TOÁN".equals(order.getStatus())) {
            Map<String, Object> res = new HashMap<>();
            res.put("status", "fail");
            res.put("message", "Đơn hàng này không thể thanh toán (trạng thái: " + order.getStatus() + ")");
            return ResponseEntity.ok(res);
        }

        String newTxnRef = txnRef + "_retry_" + System.currentTimeMillis();

        double total = order.getOrderTotal();

        Map<String, String> vnp_Params = new TreeMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", VnpayConfig.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf((long) (total * 100)));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", txnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toán đơn hàng #" + order.getOrderID());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VnpayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", request.getRemoteAddr());

        String vnp_CreateDate = new SimpleDateFormat("yyyyMMddHHmmss")
                .format(Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7")).getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (int i = 0; i < fieldNames.size(); i++) {
            String field = fieldNames.get(i);
            String value = vnp_Params.get(field);
            if (value != null && !value.isEmpty()) {
                hashData.append(field).append('=').append(URLEncoder.encode(value, "UTF-8"));
                query.append(field).append('=').append(URLEncoder.encode(value, "UTF-8"));
                if (i < fieldNames.size() - 1) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String secureHash = VnpayConfig.hmacSHA512(VnpayConfig.vnp_HashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        String paymentUrl = VnpayConfig.vnp_PayUrl + "?" + query.toString();

        Map<String, Object> res = new HashMap<>();
        res.put("status", "success");
        res.put("paymentUrl", paymentUrl);
        res.put("orderId", order.getOrderID());
        res.put("txnRef", txnRef);

        return ResponseEntity.ok(res);
    }

    @PostMapping("/vnpay-cancel/{txnRef}")//nếu kh hủy thanh toán
    public ResponseEntity<?> vnpayCancelPayment(@PathVariable String txnRef) {
        try {
            Order order = orderService.findByTxnRef(txnRef);

            if (order == null) {
                Map<String, Object> res = new HashMap<>();
                res.put("status", "error");
                res.put("message", "Không tìm thấy đơn hàng");
                return ResponseEntity.ok(res);
            }

            if ("CHỜ THANH TOÁN".equals(order.getStatus())) {
                order.setStatus("THANH TOÁN THẤT BẠI");
                orderService.save(order);

                System.out.println("Cập nhật Order " + order.getOrderID() + " thành THANH TOÁN THẤT BẠI");
            }

            Map<String, Object> res = new HashMap<>();
            res.put("status", "ok");
            res.put("message", "Đã cập nhật trạng thái");
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> res = new HashMap<>();
            res.put("status", "error");
            res.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.ok(res);
        }
    }

    @GetMapping("/check-payment-status/{txnRef}")//check trạng thái thanh toán
    public ResponseEntity<?> checkPaymentStatus(@PathVariable String txnRef) {
        try {
            Order order = orderService.findByTxnRef(txnRef);

            if (order == null) {
                Map<String, Object> res = new HashMap<>();
                res.put("status", "not_found");
                return ResponseEntity.ok(res);
            }

            Map<String, Object> res = new HashMap<>();
            res.put("status", "ok");
            res.put("orderStatus", order.getStatus());
            res.put("orderID", order.getOrderID());
            res.put("orderTotal", order.getOrderTotal());
            res.put("paymentMethod", order.getPaymentMethod());

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> res = new HashMap<>();
            res.put("status", "error");
            res.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.ok(res);
        }
    }

    @GetMapping("/vnpay-return")//trả veef link thanh toán thành công với trạng thái chờ duyệt
    @ResponseBody
    public ResponseEntity<Map<String, Object>> vnpayReturn(HttpServletRequest request) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        Account account = null;
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, String> params = new HashMap<>();
            request.getParameterMap().forEach((k, v) -> {
                if (v != null && v.length > 0) params.put(k, v[0]);
            });

            String vnpSecureHash = params.remove("vnp_SecureHash");
            params.remove("vnp_SecureHashType");

            Map<String, String> vnpParams = params.entrySet().stream()
                    .filter(e -> e.getKey() != null && e.getKey().startsWith("vnp_"))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < fieldNames.size(); i++) {
                String key = fieldNames.get(i);
                String value = vnpParams.get(key);
                String encoded = value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
                sb.append(key).append('=').append(encoded);
                if (i < fieldNames.size() - 1) sb.append('&');
            }
            String hashData = sb.toString();

            String signValue = VnpayConfig.hmacSHA512(VnpayConfig.vnp_HashSecret, hashData);

            if (vnpSecureHash != null && signValue.equalsIgnoreCase(vnpSecureHash)) {
                String responseCode = vnpParams.get("vnp_ResponseCode");
                String txnRef = vnpParams.get("vnp_TxnRef");

                if ("00".equals(responseCode)) {
                    Order order = orderService.findByTxnRef(txnRef);
                    if (order == null) {
                        System.out.println("Không tìm thấy order với txnRef = " + txnRef);
                    }
                    if (order != null) {
                        order.setStatus("Chờ duyệt");
                        orderService.save(order);
                        emailService.sendOrderEmailAsync(order.getAccount().getEmail(), order);
                    }

                    Set<OrderDetail> orderDetails = order.getOrderDetails();
                    List<Map<String, Object>> products = new ArrayList<>();

                    if (orderDetails != null && !orderDetails.isEmpty()) {
                        for (OrderDetail detail : orderDetails) {
                            Map<String, Object> product = new HashMap<>();
                            product.put("productId", detail.getProduct().getProductID());
                            product.put("productName", detail.getProduct().getProductName());
                            product.put("amount", detail.getAmount());
                            product.put("price", detail.getPrice());
                            product.put("total", detail.getPrice() * detail.getAmount());
                            products.add(product);
                        }
                    }

                    result.put("products", products);
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        session.removeAttribute("cart");
                        session.removeAttribute("currentStep");
                        session.removeAttribute("discountedTotal");
                        session.removeAttribute("currentDiscount");
                        session.removeAttribute("currentTxnRef");
                    }

                    result.put("status", "success");
                    result.put("message", "Thanh toán thành công");
                } else {
                    Order order = orderService.findByTxnRef(txnRef);
                    if (order != null) {
                        order.setStatus("THANH TOÁN THẤT BẠI");
                        orderService.save(order);
                    }

                    result.put("status", "failed");
                    result.put("message", "Thanh toán thất bại, mã lỗi: " + responseCode);
                }
                //result.put("amount", order != null ? order.getOrderTotal() : vnpParams.get("vnp_Amount"));
                result.put("amount", vnpParams.get("vnp_Amount"));
                result.put("orderId", vnpParams.get("vnp_TxnRef"));
            } else {
                result.put("status", "error");
                result.put("message", "Sai chữ ký (checksum)");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            result.put("status", "error");
            result.put("message", "Lỗi xử lý: " + ex.getMessage());
        }
        return ResponseEntity.ok(result);
    }
}
