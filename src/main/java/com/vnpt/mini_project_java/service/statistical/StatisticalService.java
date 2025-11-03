package com.vnpt.mini_project_java.service.statistical;

import com.vnpt.mini_project_java.projections.StatisticalForMonthProjections;
import com.vnpt.mini_project_java.projections.StatisticalForQuarterProjections;
import com.vnpt.mini_project_java.projections.StatisticalForYearProjections;
import com.vnpt.mini_project_java.projections.StatisticalProductProjections;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StatisticalService {
    List<StatisticalForYearProjections> statisticalForYear();

    List<StatisticalProductProjections> statisticalForProduct();

    List<StatisticalForMonthProjections> statisticalForMonth();

    List<StatisticalForQuarterProjections> statisticalForQuarter();

    List<Map<String, Object>> getProfitByDate(LocalDate startDate, LocalDate endDate);
}
