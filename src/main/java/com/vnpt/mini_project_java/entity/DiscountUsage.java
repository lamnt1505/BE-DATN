package com.vnpt.mini_project_java.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "discount_usage", schema = "public")
@Getter
@Setter
public class DiscountUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id")
    private Discount discount;

    private Double discountedAmount;

    private LocalDateTime usedAt;
}
