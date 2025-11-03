package com.vnpt.mini_project_java.restcontroller;


import com.vnpt.mini_project_java.dto.DiscountDTO;
import com.vnpt.mini_project_java.entity.Discount;
import com.vnpt.mini_project_java.service.discount.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping(value = "/api/v1/discounts", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "http://localhost:3000,http://localhost:3005", allowCredentials = "true")
@Component
public class DiscountRestController {

    @Autowired
    private DiscountService discountService;

    @Autowired
    private HttpSession session;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateDiscountCode(@RequestBody DiscountDTO discountDTO, HttpSession session) {
        try {
            Discount discount = discountService.createDiscountCode(discountDTO);

            session.setAttribute("generatedDiscountCode", discount.getDiscountCode());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("discountCode", discount.getDiscountCode());
            response.put("message", "Tạo mã giảm giá thành công!");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateDiscount(@PathVariable Long id, @RequestBody DiscountDTO discountDTO) {

        Map<String, Object> response = new HashMap<>();
        try {
            Discount updated = discountService.updateDiscount(id, discountDTO);
            if (updated == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy mã giảm giá để cập nhật!");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("success", true);
            response.put("message", "Cập nhật mã giảm giá thành công!");
            response.put("data", new DiscountDTO(
                    updated,
                    updated.getDiscountID(),
                    updated.getDiscountName(),
                    updated.getDiscountPercent(),
                    updated.getDateStart().toString(),
                    updated.getDateFinish().toString(),
                    updated.getDiscountCode(),
                    updated.getProduct() != null ? updated.getProduct().getProductID() : null
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi khi cập nhật mã giảm giá!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteDiscount(@PathVariable("id") Long id) {
        Map<String, Object> response = new HashMap<>();

        boolean deleted = discountService.deleteDiscount(id);
        if (!deleted) {
            response.put("success", false);
            response.put("message", "Không tìm thấy mã giảm giá để xóa!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.put("success", true);
        response.put("message", "Xóa mã giảm giá thành công!");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/get")
    public ResponseEntity<Map<String, Object>> getDiscountById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Discount discount = discountService.findById(id);
            if (discount == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy mã giảm giá!");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            DiscountDTO dto = new DiscountDTO(
                    discount,
                    discount.getDiscountID(),
                    discount.getDiscountName(),
                    discount.getDiscountPercent(),
                    discount.getDateStart().toString(),
                    discount.getDateFinish().toString(),
                    discount.getDiscountCode(),
                    discount.getProduct() != null ? discount.getProduct().getProductID() : null
            );

            response.put("success", true);
            response.put("data", dto);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy chi tiết mã giảm giá!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/getDiscountCode")
    public ResponseEntity<Map<String, Object>> getDiscountCode(HttpSession session) {
        String discountCode = (String) session.getAttribute("generatedDiscountCode");
        Map<String, Object> response = new HashMap<>();
        if (discountCode != null) {
            response.put("success", true);
            response.put("discountCode", discountCode);
        } else {
            response.put("success", false);
            response.put("message", "Không có mã giảm giá nào trong phiên hiện tại.");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> getLatestDiscount() {
        Map<String, Object> response = new HashMap<>();

        Optional<Discount> latestOpt = discountService.getLatestDiscount();

        if (latestOpt.isPresent()) {
            Discount latest = latestOpt.get();
            response.put("success", true);
            response.put("discountCode", latest.getDiscountCode());
            response.put("discountName", latest.getDiscountName());
            response.put("discountPercent", latest.getDiscountPercent());
            response.put("dateStart", latest.getDateStart());
            response.put("dateFinish", latest.getDateFinish());
        } else {
            response.put("success", false);
            response.put("message", "Không có mã giảm giá nào hiện tại.");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    public List<DiscountDTO> getAllDiscounts() {
        return discountService.getAllDiscounts();
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleActive(@PathVariable Long id) {
        try {
            String message = discountService.toggleActive(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi khi thay đổi trạng thái mã giảm giá!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
