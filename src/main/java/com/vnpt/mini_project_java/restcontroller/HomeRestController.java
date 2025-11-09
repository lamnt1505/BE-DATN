package com.vnpt.mini_project_java.restcontroller;

import com.cloudinary.Cloudinary;
import com.vnpt.mini_project_java.config.VnpayConfig;
import com.vnpt.mini_project_java.dto.*;
import com.vnpt.mini_project_java.entity.*;
import com.vnpt.mini_project_java.respository.DiscountRepository;
import com.vnpt.mini_project_java.respository.DiscountUsageRepository;
import com.vnpt.mini_project_java.service.account.AccountService;
import com.vnpt.mini_project_java.service.discount.DiscountService;
import com.vnpt.mini_project_java.service.favorite.FavoriteService;
import com.vnpt.mini_project_java.service.order.OrderService;
import com.vnpt.mini_project_java.service.orderDetail.OrderDetailService;
import com.vnpt.mini_project_java.service.product.ProductService;
import com.vnpt.mini_project_java.service.productvotes.ProductVotesService;
import com.vnpt.mini_project_java.service.storage.StorageService;
import com.vnpt.mini_project_java.spec.ProductSpecifications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "https://fe-qlbh-site.onrender.com", allowCredentials = "true")
public class HomeRestController {

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
    private FavoriteService favoriteService;

    @Autowired
    ProductVotesService productVotesService;

    @Autowired
    private DiscountService discountService;

    @Autowired
    DiscountRepository discountRepository;

    @Autowired
    DiscountUsageRepository discountUsageRepository;

    @Autowired
    private Cloudinary cloudinary;

    private void logToConsoleAndFile(String message) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info(message);
    }

    @GetMapping(value ="/dossier-statistic/list--Product")
    public ResponseEntity<?> showListProduct() {
        return ResponseEntity.ok(this.productService.getAllProductDTO());
    }

    @PostMapping(value ="/dossier-statistic/list--ProductById--Category--Filter/{categoryID}")
    @ResponseBody
    public List<ProductDTO> showListProductByIdCategory(@PathVariable("categoryID") long id) {
        List<Product> productList = this.productService.showListProductByIdCategoryFilter(id);
        List<ProductDTO> productDTOList = new ArrayList<>();
        for (Product product : productList) {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(product.getProductID());
            productDTO.setName(product.getProductName());
            productDTO.setDescription(product.getDescription());
            productDTO.setImage(product.getImage());
            productDTO.setPrice(product.getPrice());
            productDTO.setCategoryID(product.getCategory().getCategoryID());
            productDTOList.add(productDTO);
        }
        return productDTOList;
    }

    @GetMapping(value ="/dossier-statistic/list--Product--PriceDesc")
    public ResponseEntity<List<ProductDTO>> showListProductPriceDesc() {
        List<Product> productList = this.productService.listProductPriceDesc();
        List<ProductDTO> productDTOList = convertToDTOList(productList);
        return ResponseEntity.ok(productDTOList);
    }

    @GetMapping(value ="/dossier-statistic/list--Product--PriceAsc")
    public ResponseEntity<List<ProductDTO>> showListProductPriceAsc() {
        List<Product> productList = this.productService.listProductPriceAsc();
        List<ProductDTO> productDTOList = convertToDTOList(productList);
        return ResponseEntity.ok(productDTOList);
    }
    //2 cái này sử dụng cho list--Product--PriceAsc và list--Product--PriceDesc nhé a/c
    private List<ProductDTO> convertToDTOList(List<Product> productList) {
        List<ProductDTO> productDTOList = new ArrayList<>();
        for (Product product : productList) {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(product.getProductID());
            productDTO.setName(product.getProductName());
            productDTO.setImage(product.getImage());
            productDTO.setPrice(product.getPrice());
            productDTOList.add(productDTO);
        }
        return productDTOList;
    }

    @GetMapping(value ="/dossier-statistic/list--Product--NewBest")
    public ResponseEntity<List<ProductDTO>> showListProductNewBest() {
        List<Product> productList = this.productService.listProductNewBest();

        List<ProductDTO> productDTOList = new ArrayList<>();

        for (Product product : productList) {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(product.getProductID());
            productDTO.setName(product.getProductName());
            productDTO.setImage(product.getImage());
            productDTO.setPrice(product.getPrice());

            productDTOList.add(productDTO);
        }

        return ResponseEntity.ok(productDTOList);
    }

    @PostMapping(value ="/dossier-statistic/insert-product")
    @ResponseBody
    public Product.CartUpdateStatus saveCartToSession(@RequestParam(name = "productID") long productID,
                                                      @RequestParam int amount, HttpSession session, HttpServletRequest request) {
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
        String username = (accountId != -1) ? accountService.findById(accountId).get().getAccountName() : "unknown";
        String productName = productOrder.getProductName();
        String logMessage = "Người dùng '" + username + "' đã mua " + amount + " Đơn Vị Sản Phẩm '" + productName
                + "' Vào Gio Hang.";
        logToConsoleAndFile(logMessage);
        return Product.CartUpdateStatus.SUCCESS;
    }

    @PostMapping(value = "/dossier-statistic/update--quantities")
    @ResponseBody
    public String updateQuantity(@RequestParam(name = "productID") long productID,
                                 @RequestParam(name = "amount") int amount, HttpSession session) {
        if (amount < 0) {
            return "0";
        } else if (amount == 0) {
            List<Product> list = (List<Product>) session.getAttribute("cart");
            for (int i = 0; i < list.size(); i++) {
                if (productID == list.get(i).getProductID()) {
                    list.remove(i);
                    session.setAttribute("cart", list);
                    return "2";
                }
            }
        } else if (session.getAttribute("cart") != null) {
            List<Product> list = (List<Product>) session.getAttribute("cart");
            for (int i = 0; i < list.size(); i++) {
                if (productID == list.get(i).getProductID()) {
                    list.get(i).setAmount(amount);
                    session.setAttribute("cart", list);
                    return "1";
                }
            }
        } else {
            return "0";
        }
        return "0";
    }

    @PostMapping(value = "/dossier-statistic/orders")
    @ResponseBody
    public String orders(HttpServletRequest request, HttpSession session, @RequestBody OrderRequestDTO orderRequest) {
        Logger logger = LoggerFactory.getLogger(this.getClass());

        Account account = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accountName")) {
                    account = this.accountService.findByname(cookie.getValue()).orElse(null);
                    break;
                }
            }
        }
        if (account == null || account.getAccountID() <= 0) {
            logger.error("Account not found or invalid.");
            return "0";
        }

        List<Product> list = (List<Product>) session.getAttribute("cart");
        if (list == null || list.isEmpty()) {
            logger.error("Cart is empty or null.");
            return "-1";
        }

        Double discountedTotal = (Double) session.getAttribute("discountedTotal");
        if (discountedTotal == null) {
            discountedTotal = 0.0;
            for (Product product : list) {
                discountedTotal += product.getPrice() * product.getAmount();
            }
        }

        Order order = new Order();
        long millis = System.currentTimeMillis();
        java.sql.Date date = new java.sql.Date(millis);
        LocalDate localDate = date.toLocalDate();

        String txnRef = String.valueOf(System.currentTimeMillis());

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

        orderService.save(order);

        Set<OrderDetail> setDetail = new HashSet<>();
        for (Product product : list) {
            OrderDetail s = new OrderDetail();
            s.setProduct(product);
            s.setAmount(product.getAmount());
            s.setPrice(product.getPrice());
            s.setOrder(order);
            setDetail.add(s);

            orderDetailService.save(s);
        }
        order.setOrderDetails(setDetail);

        logger.info("Created Order ID {} with txnRef {}", order.getOrderID(), txnRef);

        session.setAttribute("cart", new ArrayList<>());
        session.removeAttribute("discountedTotal");

        return "1";
    }

    @PostMapping(value ="/dossier-statistic/cancel-order")
    public ResponseEntity<?> cancelOrder(@RequestParam(name = "orderID") Long orderID) {
        try {
            Order order = orderService.findById(orderID);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy đơn hàng");
            }
            if (!"Chờ duyệt".equals(order.getStatus())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Đơn hàng không thể hủy bỏ");
            }

            order.setStatus("Đã Hủy");
            orderService.save(order);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đơn hàng đã được hủy thành công");
            response.put("orderID", orderID);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi");
        }
    }

    @PostMapping(value ="/dossier-statistic/search")
    public ResponseEntity<?> searchProducts(@RequestBody ProductSearchCriteriaDTO criteria, Pageable pageable) {
        try {
            Specification<Product> spec = ProductSpecifications.searchByCriteria(criteria);

            Page<Product> products = productService.findAll(spec, pageable);

            Page<ProductDTO> productDTOs = products.map(ProductDTO::new);

            return ResponseEntity.ok(productDTOs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Đã xảy ra lỗi khi xử lý yêu cầu của bạn\n");
        }
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

    @PostMapping(value = "/dossier-statistic/--update-status")
    @ResponseBody
    public Order.UpdateStatus updateOrderStatus(@RequestParam(name = "orderid") Long orderID,
                                    @RequestParam(name = "status") String status) {
        Order order = orderService.findById(orderID);
        if (order == null) {
            return Order.UpdateStatus.ORDERID_NOT_FOUND;
        }
        order.setStatus(status);
        for (OrderDetail detail : order.getOrderDetails()) {
            Storage storageProduct = storageService.findQuatityProduct(detail.getProduct().getProductID());
            if (storageProduct == null) {
                return Order.UpdateStatus.STORAGE_NOT_FOUND;
            }
            if (storageProduct.getQuantity() < detail.getAmount()) {
                return Order.UpdateStatus.INSUFFICIENT_QUANTITY;
            }
        }
        if (status.equals("Hoàn thành")) {
            for (OrderDetail detail : order.getOrderDetails()) {
                Storage storageProduct = storageService.findQuatityProduct(detail.getProduct().getProductID());

                if (storageProduct != null) {
                    storageProduct.setQuantity(storageProduct.getQuantity() - detail.getAmount());
                    storageService.save(storageProduct);
                } else {
                    return Order.UpdateStatus.STORAGE_NOT_FOUND;
                }
            }
        }
        orderService.save(order);
        return Order.UpdateStatus.SUCCESS;
    }

    @PostMapping(value ="/dossier-statistic/add--favorite")
    public ResponseEntity<String> addToFavorite(@RequestParam(required = false) Long accountID,
                                                @RequestParam(required = false) Long productID) {
        if (accountID == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("⚠️ Bạn cần đăng nhập để thêm sản phẩm yêu thích!");
        }

        if (productID == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Thiếu mã sản phẩm!");
        }

        String result = favoriteService.addProductToFavorite(accountID, productID);
        HttpStatus status = result.contains("Đã thêm") ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(result, status);
    }

    @GetMapping(value ="/dossier-statistic/list--favorite")
    public ResponseEntity<List<FavoriteDTO>> getFavoriteList(@RequestParam Long accountID) {
        List<FavoriteDTO> favoriteProducts = favoriteService.getFavoritesByAccountId(accountID);
        if (favoriteProducts.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(favoriteProducts, HttpStatus.OK);
    }

    @DeleteMapping("/dossier-statistic/{accountId}/{productId}")
    public ResponseEntity<?> removeFavorite(@PathVariable Long accountId, @PathVariable Long productId) {
        try {
            boolean removed = favoriteService.removeFavorite(accountId, productId);

            Map<String, Object> response = new HashMap<>();
            if (removed) {
                response.put("success", true);
                response.put("message", "Đã xóa sản phẩm khỏi danh sách yêu thích");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy sản phẩm trong danh sách yêu thích");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping(value = "/dossier-statistic/add--vote")
    public ResponseEntity<?> createVote(@RequestBody ProductVoteDTO voteDTO) {
        try {
            ProductVote vote = productVotesService.saveVote(voteDTO);
            ProductVoteDTO responseDTO = convertToDTO(vote);
            return ResponseEntity.ok(responseDTO);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Không thể lưu đánh giá.");
        }
    }

    @PostMapping(value ="/dossier-statistic/apply")
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

    @PostMapping("/orders/vnpay")
    @ResponseBody
    public ResponseEntity<?> createOrderVnpay(@RequestBody OrderRequestDTO request, HttpServletRequest httpRequest,
                                              HttpSession session) {
        Account account = null;
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accountName".equals(cookie.getName())) {
                    account = this.accountService.findByname(cookie.getValue()).orElse(null);
                    break;
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

        double total = cart.stream().mapToDouble(p -> p.getPrice() * p.getAmount()).sum();
        String txnRef = String.valueOf(System.currentTimeMillis());

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

        orderService.save(order);

        Set<OrderDetail> setDetail = new HashSet<>();
        for (Product product : cart) {
            OrderDetail s = new OrderDetail();
            s.setProduct(product);
            s.setAmount(product.getAmount());
            s.setPrice(product.getPrice());
            s.setOrder(order);
            setDetail.add(s);

            orderDetailService.save(s);
        }
        order.setOrderDetails(setDetail);

        session.setAttribute("cart", new ArrayList<>());
        session.removeAttribute("cart");

        Map<String, Object> res = new HashMap<>();
        res.put("status", "success");
        res.put("orderId", order.getOrderID());
        res.put("txnRef", txnRef);
        res.put("total", total);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/create-payment")
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

        double total = order.getOrderTotal();

        Map<String, String> vnp_Params = new HashMap<>();
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

    @GetMapping("/vnpay-return")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> vnpayReturn(HttpServletRequest request) {
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
                System.out.println("ResponseCode: " + vnpParams.get("vnp_ResponseCode"));
                System.out.println("TxnRef: " + vnpParams.get("vnp_TxnRef"));
                System.out.println("SecureHash (VNPay): " + vnpSecureHash);
                System.out.println("SignValue (server): " + signValue);
                if ("00".equals(responseCode)) {
                    Order order = orderService.findByTxnRef(txnRef);
                    if (order == null) {
                        System.out.println("Không tìm thấy order với txnRef = " + txnRef);
                    }
                    if (order != null) {
                        order.setStatus("Chờ duyệt");
                        orderService.save(order);
                        System.out.println("Cập nhật Order ID " + order.getOrderID() + " thành ĐÃ THANH TOÁN");
                    }

                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        session.removeAttribute("cart");
                        session.removeAttribute("currentStep");
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

    public ProductVoteDTO convertToDTO(ProductVote vote) {
        ProductVoteDTO dto = new ProductVoteDTO();
        dto.setProductVoteID(vote.getProductVoteID());
        dto.setRating(vote.getRating());
        dto.setComment(vote.getComment());
        if (vote.getAccount() != null) {
            dto.setAccountID(vote.getAccount().getAccountID());
        } else {
            dto.setAccountID(null);
        }
        dto.setProductID(vote.getProduct().getProductID());
        dto.setCreatedAt(vote.getCreatedAt());
        dto.setUpdatedAt(vote.getUpdatedAt());
        return dto;
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
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

    @GetMapping("/dossier-statistic/products")
    public ResponseEntity<Page<ProductDTO>> getPaginatedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "productID,asc") String[] sort) {

        String[] sortParams = sort.clone();
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1
                ? Sort.Direction.fromString(sortParams[1])
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

        Page<ProductDTO> products = productService.getPaginatedProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/payment-method")
    public List<PaymentStatisticDTO> getPaymentStatistics() {
        return orderService.getPaymentStatistics();
    }
}

