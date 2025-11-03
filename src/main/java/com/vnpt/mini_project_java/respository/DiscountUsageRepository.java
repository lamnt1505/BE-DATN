package com.vnpt.mini_project_java.respository;

import com.vnpt.mini_project_java.entity.DiscountUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DiscountUsageRepository extends JpaRepository<DiscountUsage, Long> {
    @Query("SELECT du.discount.discountName, COUNT(du), SUM(du.discountedAmount) " +
            "FROM DiscountUsage du GROUP BY du.discount.discountName ORDER BY COUNT(du) DESC")
    List<Object[]> getDiscountStatistics();
}
