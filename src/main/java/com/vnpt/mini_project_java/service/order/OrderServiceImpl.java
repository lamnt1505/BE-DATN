package com.vnpt.mini_project_java.service.order;

import com.vnpt.mini_project_java.dto.DailyRevenueStatusDTO;
import com.vnpt.mini_project_java.dto.OrderaddressDTO;
import com.vnpt.mini_project_java.dto.PaymentStatisticDTO;
import com.vnpt.mini_project_java.dto.RevenueDTO;
import com.vnpt.mini_project_java.entity.Order;
import com.vnpt.mini_project_java.respository.OrderRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    public final OrderRepository orderRepository;

    @Override
    public void deleteById(Long orderId) {
        orderRepository.deleteById(orderId);
    }
    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public List<Order> listInvoiceByAccount(long orderID) {
        return orderRepository.findOrderByAccount(orderID);
    }

    @Override
    public <S extends Order> S save(S entity) {
        return orderRepository.save(entity);
    }
    @Override
    public List<Order> listOrder() {
        return orderRepository.findAll();
    }

    @Override
    public Order findById(Long orderID) {
        return orderRepository.findById(orderID).orElse(null);
    }

    @Override
    public void delete(Order entity) {
        orderRepository.delete(entity);
    }

    @Override
    public Optional<Order> findByOrderCode(String code) {
        return orderRepository.findByOrderCode(code);
    }

    @Override
    public Map<String, Object> getRevenue() {
        int year = LocalDate.now().getYear();
        List<Order> orders = orderRepository.findByOrderDateImportBetween(
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 12, 31)
        );

        Map<String, Double> monthlyRevenue = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) {
            monthlyRevenue.put("T" + i, 0.0);
        }

        for (Order order : orders) {
            if (order.getOrderDateImport() != null) {
                int month = order.getOrderDateImport().getMonthValue();
                monthlyRevenue.computeIfPresent("T" + month,
                        (k, v) -> v + order.getOrderTotal());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("months", new ArrayList<>(monthlyRevenue.keySet()));
        result.put("values", new ArrayList<>(monthlyRevenue.values()));
        return result;
    }

    @Override
    public List<RevenueDTO> getRevenueByMonth() {
        List<Object[]> results = orderRepository.getRevenueByMonthNative();
        List<RevenueDTO> revenues = new ArrayList<>();
        for (Object[] row : results) {
            String month = (String) row[0];
            Double revenue = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            revenues.add(new RevenueDTO(month, revenue));
        }
        return revenues;
    }

    @Override
    public List<DailyRevenueStatusDTO> getRevenueByDayAndStatus() {
        List<Object[]> results = orderRepository.getRevenueByDayAndStatus();
        return results.stream()
                .map(r -> new DailyRevenueStatusDTO(
                        (String) r[0],
                        (String) r[1],
                        ((Number) r[2]).doubleValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public OrderaddressDTO getOrderaddressById(Long orderId){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));

        OrderaddressDTO dto = new OrderaddressDTO();
        dto.setOrderID(order.getOrderID());
        dto.setOrderDateImport(order.getOrderDateImport());
        dto.setStatus(order.getStatus());
        dto.setReceiverName(order.getReceiverName());
        dto.setReceiverPhone(order.getReceiverPhone());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setNote(order.getNote());
        dto.setUsername(order.getAccount().getUsername());
        dto.setEmail(order.getAccount().getEmail());
        return dto;
    }

    @Override
    public Order findByTxnRef(String txnRef) {
        return orderRepository.findByTxnRef(txnRef).orElse(null);

    }

    @Override
    public List<PaymentStatisticDTO> getPaymentStatistics() {
        return orderRepository.getPaymentStatistics();
    }

    @Override
    public List<Map<String, Object>> getProfitByDate(LocalDate startDate, LocalDate endDate) {
        return orderRepository.getProfitStatisticsByDate(startDate, endDate);
    }
}
