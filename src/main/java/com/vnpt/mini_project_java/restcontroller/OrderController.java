package com.vnpt.mini_project_java.restcontroller;

import com.vnpt.mini_project_java.dto.*;
import com.vnpt.mini_project_java.entity.*;
import com.vnpt.mini_project_java.respository.DiscountRepository;
import com.vnpt.mini_project_java.respository.DiscountUsageRepository;
import com.vnpt.mini_project_java.service.account.AccountService;
import com.vnpt.mini_project_java.service.discount.DiscountService;
import com.vnpt.mini_project_java.service.email.EmailService;
import com.vnpt.mini_project_java.service.favorite.FavoriteService;
import com.vnpt.mini_project_java.service.order.OrderService;
import com.vnpt.mini_project_java.service.orderDetail.OrderDetailService;
import com.vnpt.mini_project_java.service.product.ProductService;
import com.vnpt.mini_project_java.service.productvotes.ProductVotesService;
import com.vnpt.mini_project_java.service.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class OrderController {
    @Autowired
    private EmailService emailService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ProductService productService;

    @Autowired
    private StorageService storageService;

    @Autowired
    ProductVotesService productVotesService;

    @Autowired
    private DiscountService discountService;

    @Autowired
    DiscountRepository discountRepository;

    @Autowired
    DiscountUsageRepository discountUsageRepository;

    private void logToConsoleAndFile(String message) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info(message);
    }

    @PostMapping(value ="/dossier-statistic/insert-product")
    @ResponseBody
    public Product.CartUpdateStatus saveCartToSession(@RequestParam(name = "productID") long productID,
                                                      @RequestParam int amount, HttpSession session,
                                                      HttpServletRequest request) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        Product productOrder = this.productService.findByIdProduct(productID);
        if (productOrder == null) {
            return Product.CartUpdateStatus.PRODUCT_NOT_FOUND;
        }
        if (amount <= 0) {
            return Product.CartUpdateStatus.INVALID_AMOUNT;
        }
        List<Product> cart = (List<Product>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }
        boolean productFoundInCart = false;
        for (Product item : cart) {
            if (item.getProductID().equals(productOrder.getProductID())) {
                item.setAmount(item.getAmount() + amount);
                productFoundInCart = true;
                break;
            }
        }
        if (!productFoundInCart) {
            productOrder.setAmount(amount);
            cart.add(productOrder);
        }
        session.setAttribute("currentStep", 1);
        Cookie[] cookies = request.getCookies();
        long accountId = -1;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accountName")) {
                    Account account = this.accountService.findByname(cookie.getValue()).orElse(null);
                    if (account != null) {
                        accountId = account.getAccountID();
                    }
                    break;
                }
            }
        }
        String username = (accountId != -1) ? accountService.findById(accountId).get().getAccountName() : "Khách Hàng";
        String productName = productOrder.getProductName();
        String logMessage = "Người dùng '" + username + "' đã mua " + amount + " Đơn Vị Sản Phẩm '" + productName
                + "' Vào Giỏ Hàng";
        logToConsoleAndFile(logMessage);
        logger.info(logMessage);
        return Product.CartUpdateStatus.SUCCESS;
    }

    @PostMapping(value = "/dossier-statistic/update--quantities")
    @ResponseBody
    public String updateQuantity(@RequestParam(name = "productID") long productID,
                                 @RequestParam(name = "amount") int amount, HttpSession session) {
        if (amount < 0) {// trả về lỗi
            return "0";
        } else if (amount == 0) {// xóa sản phẩm khỏi gi hàng
            List<Product> list = (List<Product>) session.getAttribute("cart");
            for (int i = 0; i < list.size(); i++) {
                if (productID == list.get(i).getProductID()) {
                    list.remove(i);// Xóa sản phẩm
                    session.setAttribute("cart", list);// Cập nhật session
                    return "2";// thành cong
                }
            }
        } else if (session.getAttribute("cart") != null) {
            List<Product> list = (List<Product>) session.getAttribute("cart");
            for (int i = 0; i < list.size(); i++) {
                if (productID == list.get(i).getProductID()) {
                    list.get(i).setAmount(amount);// cap nhat so luong
                    session.setAttribute("cart", list);
                    return "1";
                }
            }
        } else {
            return "0";//gior hang rong
        }
        return "0";//Không tìm thấy sản phẩm
    }

    @PostMapping(value = "/dossier-statistic/orders")
    @ResponseBody
    public String orders(HttpServletRequest request, HttpSession session, @RequestBody OrderRequestDTO orderRequest) {
        Logger logger = LoggerFactory.getLogger(this.getClass());

        logger.info("========== BẮT ĐẦU TẠO ĐƠN HÀNG MỚI ==========");
        logger.info("Thời gian: {}", LocalDateTime.now());

        Account account = null;
        String accountIdHeader = request.getHeader("X-Account-ID");
        if (accountIdHeader != null && !accountIdHeader.isEmpty()) {// tìm thông tin từ header
            try {
                Long accountID = Long.parseLong(accountIdHeader);
                account = this.accountService.findById(accountID).orElse(null);
                logger.info("Found account from header: {}", accountID);
            } catch (Exception e) {
                logger.error("Error parsing accountID from header: {}", e.getMessage());
            }
        }

        if (account == null) {
            Cookie[] cookies = request.getCookies();// tìm thông tin từ cookie voiws accountName
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("accountName")) {
                        account = this.accountService.findByname(cookie.getValue()).orElse(null);
                        break;
                    }
                }
            }
        }
        if (account == null && session.getAttribute("accountID") != null) {
            Long accountID = (Long) session.getAttribute("accountID");
            account = this.accountService.findById(accountID).orElse(null);
        }

        if (account == null || account.getAccountID() <= 0) {// kiemer tra xem account da login hay chua
            logger.error("Account not found or invalid.");
            return "0";
        }

        logger.info("Người dùng: {} (ID: {})", account.getAccountName(), account.getAccountID());

        List<Product> list = (List<Product>) session.getAttribute("cart");
        if (list == null || list.isEmpty()) {// kiem tra gio hang
            logger.error("Cart is empty or null.");
            return "-1";
        }

        Double discountedTotal = (Double) session.getAttribute("discountedTotal");
        if (discountedTotal == null) {
            //check kiem tra co ma giam gia hay khong
            discountedTotal = 0.0;
            for (Product product : list) {
                discountedTotal += product.getPrice() * product.getAmount();// neu khong lay so luong product.getPrice() * product.getAmount()
            }
        }

        Order order = new Order();

        //lay thong tin ngay hien tai
        long millis = System.currentTimeMillis();
        java.sql.Date date = new java.sql.Date(millis);
        LocalDate localDate = date.toLocalDate();

        // ma giao dich don hang
        String txnRef = String.valueOf(System.currentTimeMillis());

        //set cac thong tin don hang
        order.setOrderDateImport(localDate);
        order.setStatus("Chờ duyệt");
        order.setOrderTotal(discountedTotal);
        order.setVendor(account);
        order.setTxnRef(txnRef);
        order.setPaymentMethod("COD");

        order.setReceiverName(orderRequest.getReceiverName());
        order.setReceiverPhone(orderRequest.getReceiverPhone());
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setNote(orderRequest.getNote());

        logger.info("Thông tin đơn hàng:");
        logger.info("-Người nhận: {}", orderRequest.getReceiverName());
        logger.info("-Phương thức thanh toán: {}", order.getPaymentMethod());
        try {
            orderService.save(order);// luu trang thai don hang
            logger.info("Lưu đơn hàng thành công (ID: {}, OrderNumber {})", order.getOrderID(), order.getOrderNumber());

            emailService.sendOrderEmailAsync(order.getAccount().getEmail(), order);// gui email don hang
            logger.info("📩 Đã gửi email xác nhận đơn hàng đến {}", account.getEmail());
        } catch (Exception e) {
            logger.error("❌ LỖI khi lưu đơn hàng: {}", e.getMessage(), e);
            return "0";
        }

        // luu thong tin chi tiet don hang
        Set<OrderDetail> setDetail = new HashSet<>();
        for (Product product : list) {
            OrderDetail s = new OrderDetail();
            s.setProduct(product);
            s.setAmount(product.getAmount());
            s.setPrice(product.getPrice());
            s.setOrder(order);
            setDetail.add(s);

            orderDetailService.save(s);// Lưu từng chi tiết vào database
            logger.debug("✅ Thêm sản phẩm '{}' (Số lượng: {}, Giá: {})",
                    product.getProductName(),
                    product.getAmount(),
                    product.getPrice());
        }
        order.setOrderDetails(setDetail);

        logger.info("Tạo {} chi tiết đơn hàng");
        logger.info("TẠO ĐƠN HÀNG THÀNH CÔNG");
        logger.info("Created Order ID {} with txnRef {}", order.getOrderID(), txnRef);
        logger.info("   - Người tạo: {} (ID: {})", account.getAccountName(), account.getAccountID());

        session.setAttribute("cart", new ArrayList<>());
        session.removeAttribute("discountedTotal");

        logger.info("========== KẾT THÚC TẠO ĐƠN HÀNG ==========\n");

        return "1";
    }

    @PostMapping(value ="/dossier-statistic/cancel-order")//API huy don hang
    public ResponseEntity<?> cancelOrder(@RequestParam(name = "orderID") Long orderID,
                                         @RequestParam(name = "reason") String reason) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        try {
            Order order = orderService.findById(orderID);// tim theo ID don hang
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy đơn hàng");
            }
            if (!"Chờ duyệt".equals(order.getStatus())) {//trang thai cho duyet moi cho huy
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Đơn hàng không thể hủy bỏ");
            }

            //luu thong tin huy
            order.setStatus("Đã Hủy");
            order.setNote(reason);
            orderService.save(order);

            // gui thong tin email
            emailService.sendCancelOrderEmailAsync(order.getAccount().getEmail(), order);
            logger.info("📧 Đã gửi email hủy đơn hàng đến {}", order.getAccount().getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đơn hàng đã được hủy thành công");
            response.put("orderID", orderID);
            response.put("reason", reason);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi");
        }
    }

    @PostMapping(value = "/dossier-statistic/--update-status")
    @ResponseBody//API cap nhat trang thai don hang
    public Order.UpdateStatus updateOrderStatus(@RequestParam(name = "orderid") Long orderID,
                                                @RequestParam(name = "status") String status) {
        Order order = orderService.findById(orderID);//tim theo id don hang
        if (order == null) {
            return Order.UpdateStatus.ORDERID_NOT_FOUND;// tra thong bao khong tim thay don hang
        }
        order.setStatus(status);
        for (OrderDetail detail : order.getOrderDetails()) {
            Storage storageProduct = storageService.findQuatityProduct(detail.getProduct().getProductID());
            if (storageProduct == null) {
                return Order.UpdateStatus.STORAGE_NOT_FOUND;//khong ton tai san pham trong kho
            }
            if (storageProduct.getQuantity() < detail.getAmount()) {
                return Order.UpdateStatus.INSUFFICIENT_QUANTITY;// so luong trong kho khong du
            }
        }
        // so luong trong kho tru khi trang thai don hang hoan thanh
        if (status.equals("Hoàn thành")) {
            for (OrderDetail detail : order.getOrderDetails()) {
                Storage storageProduct = storageService.findQuatityProduct(detail.getProduct().getProductID());

                if (storageProduct != null) {
                    storageProduct.setQuantity(storageProduct.getQuantity() - detail.getAmount());// giam so luong trong kho
                    storageService.save(storageProduct);// luu db
                } else {
                    return Order.UpdateStatus.STORAGE_NOT_FOUND;//kho khong ton tai
                }
            }
        }
        orderService.save(order);
        return Order.UpdateStatus.SUCCESS;
    }

    @PostMapping(value ="/dossier-statistic/apply")// api su dung ma giam gia
    public ResponseEntity<Map<String, Object>> applyDiscount(@RequestBody Map<String, Object> requestData,
                                                             HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {

            String discountCode = (String) requestData.get("discountCode");
            if (discountCode == null || discountCode.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Mã giảm giá không được để trống.");
                return ResponseEntity.badRequest().body(response);
            }
            Discount discount = discountRepository.findByDiscountCode(discountCode).orElse(null);
            if (discount == null) {
                response.put("success", false);
                response.put("message", "Mã giảm giá không hợp lệ hoặc không tồn tại!");
                return ResponseEntity.badRequest().body(response);
            }

            if (!discount.getActive()) {
                response.put("success", false);
                response.put("message", "Mã giảm giá đã bị vô hiệu hoá!");
                return ResponseEntity.badRequest().body(response);
            }

            LocalDate today = LocalDate.now();
            if (discount.getDateStart().isAfter(today) || discount.getDateFinish().isBefore(today)) {
                response.put("success", false);
                response.put("message", "Mã giảm giá đã hết hạn hoặc chưa tới ngày bắt đầu!");
                return ResponseEntity.badRequest().body(response);
            }
            if (!discountService.existsByCode(discountCode)) {
                response.put("success", false);
                response.put("message", "Mã giảm giá không hợp lệ hoặc đã hết hạn!");
                return ResponseEntity.badRequest().body(response);
            }

            List<Map<String, Object>> products = (List<Map<String, Object>>) requestData.get("products");
            if (products == null || products.isEmpty()) {
                response.put("success", false);
                response.put("message", "Danh sách sản phẩm không được để trống.");
                return ResponseEntity.badRequest().body(response);
            }

            List<Map<String, Object>> discountedProducts = new ArrayList<>();
            double discountedTotal = 0.0;
            double totalDiscountAmount = 0.0;

            for (Map<String, Object> product : products) {
                String productID = product.get("productID").toString();
                double price = Double.parseDouble(product.get("price").toString());
                int quantity = Integer.parseInt(product.get("quantity").toString());

                double discountedPrice = discountService.applyDiscount(price, discountCode);
                double discountAmount = (price - discountedPrice) * quantity;
                totalDiscountAmount += discountAmount;
                discountedTotal += discountedPrice * quantity;

                Map<String, Object> discountedProduct = new HashMap<>();
                discountedProduct.put("productID", productID);
                discountedProduct.put("originalPrice", price);
                discountedProduct.put("discountedPrice", discountedPrice);
                discountedProduct.put("quantity", quantity);
                discountedProducts.add(discountedProduct);
            }
            DiscountUsage usage = new DiscountUsage();
            usage.setDiscount(discount);
            usage.setDiscountedAmount(totalDiscountAmount);
            usage.setUsedAt(LocalDateTime.now());
            discountUsageRepository.save(usage);

            session.setAttribute("discountedTotal", discountedTotal);

            response.put("success", true);
            response.put("discountedProducts", discountedProducts);
            response.put("discountedTotal", discountedTotal);
            response.put("message", "Mã giảm giá hợp lệ!");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Đã xảy ra lỗi trong quá trình xử lý.");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dossier-statistic/summary")
    public ResponseEntity<List<OrderSummaryDTO>> listOrderSummary() {
        List<Order> orders = orderService.listOrder();
        List<OrderSummaryDTO> summaries = orders.stream()
                .map(o -> new OrderSummaryDTO(
                        o.getOrderID(),
                        o.getOrderDateImport(),
                        o.getAccount().getAccountName(),
                        o.getAccount().getPhoneNumber(),
                        o.getOrderTotal(),
                        o.getStatus(),
                        o.getPaymentMethod(),
                        o.getTxnRef()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/orders/account/{accountId}")
    public ResponseEntity<?> getOrdersByAccount(@PathVariable("accountId") long accountID) {
        List<Order> orders = orderService.listInvoiceByAccount(accountID);
        if (orders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "No orders found for this account"));
        }
        List<Map<String, Object>> orderList = new ArrayList<>();
        for (Order order : orders) {
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("orderId", order.getOrderID());
            orderMap.put("orderNumber", order.getOrderNumber());
            orderMap.put("orderDate", order.getOrderDateImport());
            orderMap.put("status", order.getStatus());
            orderMap.put("orderTotal", order.getOrderTotal());
            orderMap.put("txnRef", order.getTxnRef());
            List<Map<String, Object>> productOrderList = new ArrayList<>();
            for (OrderDetail orderDetail : order.getOrderDetails()) {
                Map<String, Object> productMap = new HashMap<>();
                productMap.put("productId", orderDetail.getProduct().getProductID());
                productMap.put("productName", orderDetail.getProduct().getProductName());
                productMap.put("price", orderDetail.getPrice());
                productMap.put("amount", orderDetail.getAmount());
                productOrderList.add(productMap);
            }
            orderMap.put("orderDetails", productOrderList);
            orderList.add(orderMap);
        }
        return ResponseEntity.ok(orderList);
    }

    @GetMapping("/product-cart")
    public ResponseEntity<?> getCart(HttpSession session) {
        List<Product> cart = (List<Product>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();

        List<CartItemDTO> cartDTOs = cart.stream()
                .map(CartItemDTO::new)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("cart", cartDTOs);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/{orderDetailID}")
    public ResponseEntity<?> viewOrderDetails(@PathVariable("orderDetailID") long orderDetailID,
                                              HttpServletRequest request) {
        try {
            Map<String, Object> response = new HashMap<>();

            List<OrderDetail> list = orderDetailService.findDetailByInvoiceId(orderDetailID);

            List<Map<String, Object>> productOrders = new ArrayList<>();
            for (OrderDetail od : list) {
                Product odrProduct = productService.findByIdProduct(od.getProduct().getProductID());

                Map<String, Object> productMap = new HashMap<>();
                productMap.put("productId", odrProduct.getProductID());
                productMap.put("productName", odrProduct.getProductName());
                productMap.put("price", od.getPrice());
                productMap.put("amount", od.getAmount());
                productMap.put("total", od.getPrice() * od.getAmount());
                productOrders.add(productMap);
            }
            response.put("oldOrders", productOrders);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/orders/address/{orderID}")
    public OrderaddressDTO getOrderDetail(@PathVariable Long orderID) {
        return orderService.getOrderaddressById(orderID);
    }

    @GetMapping("/address/account/{accountID}")
    public ResponseEntity<OrderaddressDTO> getAddressByAccount(@PathVariable Long accountID) {
        Account account = accountService.getAccountById(accountID);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }

        OrderaddressDTO dto = new OrderaddressDTO();
        dto.setAccountID(account.getAccountID());
        dto.setUsername(account.getUsername());
        dto.setEmail(account.getEmail());
        dto.setPhoneNumber(account.getPhoneNumber());
        dto.setLocal(account.getLocal());
        dto.setReceiverName(account.getUsername());
        dto.setReceiverPhone(account.getPhoneNumber());
        dto.setShippingAddress(account.getLocal());

        return ResponseEntity.ok(dto);
    }

    @GetMapping(value ="/dossier-statistic/cart/quantity")
    @ResponseBody
    public int getCartQuantity(HttpSession session) {
        List<Product> cart = (List<Product>) session.getAttribute("cart");
        if (cart == null) {
            return 0;
        }
        return cart.stream().mapToInt(Product::getAmount).sum();
    }

    @GetMapping("/payment-method")
    public List<PaymentStatisticDTO> getPaymentStatistics() {
        return orderService.getPaymentStatistics();
    }
}
