package com.vnpt.mini_project_java.restcontroller;

import com.vnpt.mini_project_java.projections.StatisticalForMonthProjections;
import com.vnpt.mini_project_java.projections.StatisticalForQuarterProjections;
import com.vnpt.mini_project_java.projections.StatisticalForYearProjections;
import com.vnpt.mini_project_java.projections.StatisticalProductProjections;
import com.vnpt.mini_project_java.service.statistical.StatisticalService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ReportsController {

    private final StatisticalService statisticalService;

    public ReportsController(StatisticalService statisticalService) {
        this.statisticalService = statisticalService;
    }

    @GetMapping("/reports/product")
    public ResponseEntity<byte[]> generateProductStatisticsReport(){
        List<StatisticalProductProjections> productStatistics = statisticalService.statisticalForProduct();
        try {
            InputStream reportTemplate = getClass().getResourceAsStream("/reports/productReports.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(reportTemplate);

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(productStatistics);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("dataSource", dataSource);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            byte[] reportBytes = JasperExportManager.exportReportToPdf(jasperPrint);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "product-statistics.pdf");

            return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/reports/year")
    public ResponseEntity<byte[]> generateYearStatisticsReport(){
        List<StatisticalForYearProjections> yearStatistics = statisticalService.statisticalForYear();
        try{
            InputStream reportTemplate = getClass().getResourceAsStream("/reports/YearReports.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(reportTemplate);

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(yearStatistics);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("dataSource", dataSource);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            byte[] reportBytes = JasperExportManager.exportReportToPdf(jasperPrint);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "year-statistics.pdf");

            return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
        } catch (JRException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/reports/month")
    public ResponseEntity<byte[]> generateMonthStatisticsReport(){
        List<StatisticalForMonthProjections> monthStatistics = statisticalService.statisticalForMonth();

        try{
            InputStream reportTemplate = getClass().getResourceAsStream("/reports/monthReports.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(reportTemplate);

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(monthStatistics);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("dataSource", dataSource);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            byte[] reportBytes = JasperExportManager.exportReportToPdf(jasperPrint);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "month-statistics.pdf");

            return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
        } catch (JRException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/reports/quarter")
    public ResponseEntity<byte[]> generateQuarterStatisticsReport() {

        List<StatisticalForQuarterProjections> quarterStatistics = statisticalService.statisticalForQuarter();

        try {
            InputStream reportTemplate = getClass().getResourceAsStream("/reports/quarterReports.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(reportTemplate);

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(quarterStatistics);

            Map<String, Object> parameters = new HashMap<>();

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            byte[] reportBytes = JasperExportManager.exportReportToPdf(jasperPrint);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "quarter-statistics.pdf");

            return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
