package com.vnpt.mini_project_java.service.discount;

import com.vnpt.mini_project_java.dto.DiscountDTO;
import com.vnpt.mini_project_java.entity.Discount;

import java.util.List;
import java.util.Optional;

public interface DiscountService {
    Discount createDiscountCode(DiscountDTO discountDTO);

    Discount updateDiscount(Long id, DiscountDTO dto);

    Optional<Discount> validateDiscountCode(String discountCode);

    boolean existsByCode(String code);

    double applyDiscount(double totalPrice, String discountCode);

    Optional<Discount> getLatestDiscount();

    List<DiscountDTO> getAllDiscounts();

    String toggleActive(Long id);

    Discount findById(Long id);

    boolean deleteDiscount(Long id);
}
