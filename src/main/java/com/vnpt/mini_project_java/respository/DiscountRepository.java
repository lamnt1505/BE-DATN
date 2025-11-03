package com.vnpt.mini_project_java.respository;

import com.vnpt.mini_project_java.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount,Long> {
    Optional<Discount> findByDiscountCode(String discountCode);

    Optional<Discount> findByDiscountCodeIgnoreCase(String discountCode);

    boolean existsByDiscountCodeIgnoreCase(String discountCode);

    Optional<Discount> findTopByOrderByDateStartDesc();

    @Query("SELECT d FROM Discount d " +
            "WHERE (d.dateStart <= :endDate AND d.dateFinish >= :startDate)")
    List<Discount> findOverlappingDiscounts(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    boolean existsByDiscountNameIgnoreCase(String discountName);
}
