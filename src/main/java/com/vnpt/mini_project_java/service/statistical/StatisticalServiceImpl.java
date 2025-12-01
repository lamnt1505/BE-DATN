package com.vnpt.mini_project_java.service.statistical;

import com.vnpt.mini_project_java.projections.StatisticalForMonthProjections;
import com.vnpt.mini_project_java.projections.StatisticalForQuarterProjections;
import com.vnpt.mini_project_java.projections.StatisticalForYearProjections;
import com.vnpt.mini_project_java.projections.StatisticalProductProjections;
import com.vnpt.mini_project_java.respository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@Service
public class StatisticalServiceImpl implements StatisticalService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<StatisticalForYearProjections> statisticalForYear() {
        return orderRepository.statisticalForYear();
    }

    @Override
    public List<StatisticalProductProjections> statisticalForProduct() {
        return orderRepository.statisticalForProduct();
    }

    @Override
    public List<StatisticalForMonthProjections> statisticalForMonth() {
        return orderRepository.statisticalForMonth();
    }

    @Override
    public List<StatisticalForQuarterProjections> statisticalForQuarter() {
        return orderRepository.statisticalForQuarter();
    }

    @Override
    public List<Map<String, Object>> getProfitByDate(LocalDate startDate, LocalDate endDate) {
        return orderRepository.getProfitStatisticsByDate(startDate, endDate);
    }

    @Override
    public double getTotalRevenue() {
        Double result = orderRepository.getTotalRevenue();
        return result != null ? result : 0.0;
    }
}
