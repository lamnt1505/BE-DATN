package com.vnpt.mini_project_java.dto;

import com.vnpt.mini_project_java.entity.Discount;
import lombok.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@Getter
@Setter
@ToString
public class DiscountDTO {
    private Long discountID;

    private String discountName;

    private Double discountPercent;

    private String dateStart;

    private String dateFinish;

    private String discountCode;

    private Long productID;

    private Boolean active;

    public DiscountDTO() {
    }

    public DiscountDTO(Discount discount, Long discountID, String discountName, Double discountPercent, String dateStart, String dateFinish, String discountCode, Long productID) {
        this.discountID = discountID;
        this.discountName = discountName;
        this.discountPercent = discountPercent;
        this.dateStart = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(discount.getDateStart());
        this.dateFinish = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(discount.getDateFinish());
        this.discountCode = discountCode;
        this.productID = productID;
        this.active = discount.getActive();
    }
}
