package com.vnpt.mini_project_java.respository;

import com.vnpt.mini_project_java.dto.PaymentStatisticDTO;
import com.vnpt.mini_project_java.entity.Order;
import com.vnpt.mini_project_java.projections.StatisticalForMonthProjections;
import com.vnpt.mini_project_java.projections.StatisticalForQuarterProjections;
import com.vnpt.mini_project_java.projections.StatisticalForYearProjections;
import com.vnpt.mini_project_java.projections.StatisticalProductProjections;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {

    String STATICTICAL_FOR_PRODUCT_QUERY = "SELECT od.productid AS id," +
            " p.product_name AS name," +
            " SUM(od.amount) AS quantitysold," +
            " SUM(o.order_total) AS total" +
            " FROM order_info o " +
            " JOIN order_detail od ON o.order_id = od.order_id" +
            " JOIN product p ON p.product_id = od.productid" +
            " GROUP BY od.productid";

    String STATICTICAL_FOR_YEAR_QUERY = "SELECT YEAR(order_import) AS year," +
            " COUNT(order_info.order_id) AS orderCount," +
            " SUM(order_total) AS total," +
            " MIN(order_total) AS minTotal," +
            " MAX(order_total) AS maxTotal" +
            " FROM order_info " +
            " JOIN " +
                "order_detail ON order_info.order_id = order_detail.order_id" +
            " GROUP BY " +
                "YEAR(order_import)";

    String STATISCAL_FOR_MONTH_QUERY = "SELECT MONTH(order_import) AS month, YEAR(order_import) AS year," +
            " COUNT(order_info.order_id) AS orderCount," +
            " SUM(order_total) AS total," +
            " MIN(order_total) AS minTotal," +
            " MAX(order_total) AS maxTotal" +
                " FROM order_info " +
            " JOIN order_detail ON order_info.order_id = order_detail.order_id" +
            " GROUP BY " +
            "MONTH(order_import),YEAR(order_import)";

    String STATICTICAL_FOR_QUARTER_QUERY = "SELECT YEAR(order_import) AS year, " +
            "QUARTER(order_import) AS quarter, " +
            "COUNT(order_info.order_id) AS orderCount, " +
            "SUM(order_total) AS total, " +
            "MIN(order_total) AS minTotal, " +
            "MAX(order_total) AS maxTotal " +
            "FROM order_info " +
            "JOIN order_detail ON order_info.order_id = order_detail.order_id " +
            "GROUP BY YEAR(order_import), QUARTER(order_import)";

    @Query(value = "SELECT DATE_FORMAT(order_import, '%Y-%m') as month, " +
                    "SUM(order_total) as revenue " +
                    "FROM order_info " +
                    "WHERE status = 'Hoàn thành' " +
                    "GROUP BY DATE_FORMAT(order_import, '%Y-%m') " +
                    "ORDER BY month ASC",
            nativeQuery = true
    )
    List<Object[]> getRevenueByMonthNative();

    @Query(value = "SELECT DATE_FORMAT(order_import, '%Y-%m-%d') AS day,\n" +
            "       status,\n" +
            "       SUM(order_total) AS revenue\n" +
            "FROM order_info\n" +
            "WHERE status IN ('Chờ duyệt', 'Hoàn thành')\n" +
            "GROUP BY DATE_FORMAT(order_import, '%Y-%m-%d'), status\n" +
            "ORDER BY day ASC;", nativeQuery = true)
    List<Object[]> getRevenueByDayAndStatus();


    @Query("SELECT new com.vnpt.mini_project_java.dto.PaymentStatisticDTO(o.paymentMethod, COUNT(o)) " +
            "FROM Order o " +
            "GROUP BY o.paymentMethod")
    List<PaymentStatisticDTO> getPaymentStatistics();

    @Query(value = "SELECT \n" +
            "            p.product_name AS productName,\n" +
            "            SUM(od.amount * (od.price - p.price)) AS profit,\n" +
            "            SUM(od.amount * od.price) AS revenue,\n" +
            "            SUM(od.amount * p.price) AS cost\n" +
            "        FROM order_detail od\n" +
            "        JOIN product p ON p.product_id = od.productid\n" +
            "        JOIN order_info o ON o.order_id = od.order_id\n" +
            "        WHERE o.order_import BETWEEN :startDate AND :endDate\n" +
            "        GROUP BY p.product_id\n" +
            "        ORDER BY profit DESC"
        ,
       nativeQuery = true)
    List<Map<String, Object>> getProfitStatisticsByDate(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = STATICTICAL_FOR_PRODUCT_QUERY, nativeQuery = true)
    List<StatisticalProductProjections> statisticalForProduct();

    @Query(value = STATICTICAL_FOR_YEAR_QUERY, nativeQuery = true)
    List<StatisticalForYearProjections> statisticalForYear();

    @Query(value = STATISCAL_FOR_MONTH_QUERY, nativeQuery = true)
    List<StatisticalForMonthProjections> statisticalForMonth();

    @Query(value = STATICTICAL_FOR_QUARTER_QUERY, nativeQuery = true)
    List<StatisticalForQuarterProjections> statisticalForQuarter();

    @Query(value = "select * from order_info where account_id = ?", nativeQuery = true)
    List<Order> findOrderByAccount(long orderID);

    Optional<Order> findByOrderCode(String orderCode);

    List<Order> findByOrderDateImportBetween(LocalDate start, LocalDate end);

    List<Order> findTop5ByOrderByOrderDateImportDesc();

    Optional<Order> findByTxnRef(String txnRef);
}
