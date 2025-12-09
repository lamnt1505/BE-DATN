package com.vnpt.mini_project_java.restcontroller;

import com.vnpt.mini_project_java.projections.StatisticalForMonthProjections;
import com.vnpt.mini_project_java.projections.StatisticalForQuarterProjections;
import com.vnpt.mini_project_java.projections.StatisticalForYearProjections;
import com.vnpt.mini_project_java.projections.StatisticalProductProjections;
import com.vnpt.mini_project_java.respository.OrderStatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    @Autowired
    private OrderStatisticsRepository statisticsRepository;

    @GetMapping("/product")
    public ResponseEntity<List<StatisticalProductProjections>> getProductStatistics() {
        List<StatisticalProductProjections> list = statisticsRepository.statisticalForProduct();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/year")
    public ResponseEntity<List<StatisticalForYearProjections>> getYearStatistics() {
        List<StatisticalForYearProjections> list = statisticsRepository.statisticalForYear();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/month")
    public ResponseEntity<List<StatisticalForMonthProjections>> getMonthStatistics() {
        List<StatisticalForMonthProjections> list = statisticsRepository.statisticalForMonth();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/quarter")
    public ResponseEntity<List<StatisticalForQuarterProjections>> getQuarterStatistics() {
        List<StatisticalForQuarterProjections> list = statisticsRepository.statisticalForQuarter();
        return ResponseEntity.ok(list);
    }
}
