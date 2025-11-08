package com.vnpt.mini_project_java.dto;


import java.time.LocalDate;
import lombok.Data;

@Data
public class OrderSummaryDTO {
    private Long orderId;
    private LocalDate orderDate;
    private String customerName;
    private String phoneNumber;
    private Double totalAmount;
    private String status;
    private String paymentMethod;
    private String txnRef;

    public OrderSummaryDTO(Long orderId, LocalDate orderDate, String customerName,
                           String phoneNumber, Double totalAmount, String status, String paymentMethod, String txnRef) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.txnRef = txnRef;
    }
}
