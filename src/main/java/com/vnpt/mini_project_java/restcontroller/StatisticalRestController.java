package com.vnpt.mini_project_java.restcontroller;

import com.vnpt.mini_project_java.dto.DailyRevenueStatusDTO;
import com.vnpt.mini_project_java.dto.RevenueDTO;
import com.vnpt.mini_project_java.projections.StatisticalForMonthProjections;
import com.vnpt.mini_project_java.projections.StatisticalForQuarterProjections;
import com.vnpt.mini_project_java.projections.StatisticalForYearProjections;
import com.vnpt.mini_project_java.projections.StatisticalProductProjections;
import com.vnpt.mini_project_java.respository.DiscountUsageRepository;
import com.vnpt.mini_project_java.service.order.OrderService;
import com.vnpt.mini_project_java.service.statistical.StatisticalService;
import com.vnpt.mini_project_java.util.ExcelUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/export", produces = MediaType.APPLICATION_JSON_VALUE)
public class StatisticalRestController {

    private final StatisticalService statisticsService;

    private final OrderService orderService;

    private final DiscountUsageRepository discountUsageRepository;

    public StatisticalRestController(StatisticalService statisticsService, OrderService orderService, DiscountUsageRepository discountUsageRepository) {
        this.statisticsService = statisticsService;
        this.orderService = orderService;
        this.discountUsageRepository = discountUsageRepository;
    }

    @GetMapping("/product")
    public ResponseEntity<ByteArrayResource> exportStatistics() {
        List<StatisticalProductProjections> statistics = statisticsService.statisticalForProduct();
        byte[] excelData = ExcelUtil.createStatisticalProductExcel(statistics);
        ByteArrayResource resource = new ByteArrayResource(excelData);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=statistical_product.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(resource.contentLength())
                .body(resource);
    }

    @GetMapping("/year")
    public ResponseEntity<ByteArrayResource> exportyear(){
        List<StatisticalForYearProjections> statistics = statisticsService.statisticalForYear();

        byte[] excelData = ExcelUtil.createStatisticalForYearExcel(statistics);

        ByteArrayResource resource = new ByteArrayResource(excelData);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=statistical_year.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(resource.contentLength())
                .body(resource);
    }

    @GetMapping("/month")
    public ResponseEntity<ByteArrayResource> exportmonth(){
        List<StatisticalForMonthProjections> statistical = statisticsService.statisticalForMonth();

        byte[] excelData = ExcelUtil.createStatisticalForMonth(statistical);

        ByteArrayResource resource = new ByteArrayResource(excelData);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=statistical_month.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(resource.contentLength())
                .body(resource);
    }

    @GetMapping("/revenue")
    public Map<String, Object> getRevenue() {
        return orderService.getRevenue();
    }

    @GetMapping("/revenue-by-month")
    public ResponseEntity<List<RevenueDTO>> getRevenueByMonth() {
        return ResponseEntity.ok(orderService.getRevenueByMonth());
    }

    @GetMapping("/revenue-by-day-status")
    public ResponseEntity<List<DailyRevenueStatusDTO>> getRevenueByDayAndStatus() {
        return ResponseEntity.ok(orderService.getRevenueByDayAndStatus());
    }

    @GetMapping("/quarter/export-excel")
    public void exportQuarterStatisticsToExcel(HttpServletResponse response) {
        try {
            List<StatisticalForQuarterProjections> list = statisticsService.statisticalForQuarter();

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=thong_ke_quy.xlsx");

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Thống kê theo quý");


            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 12);


            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);


            Font dataFont = workbook.createFont();
            dataFont.setFontHeightInPoints((short) 11);


            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setFont(dataFont);
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);


            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.cloneStyleFrom(dataStyle);
            DataFormat format = workbook.createDataFormat();
            moneyStyle.setDataFormat(format.getFormat("#,##0 \"₫\""));


            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BÁO CÁO THỐNG KÊ DOANH THU THEO QUÝ");
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setBold(true);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleCell.setCellStyle(titleStyle);


            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));


            Row headerRow = sheet.createRow(2);
            String[] headers = {"Năm", "Quý", "Tổng doanh thu (VNĐ)", "Số đơn hàng"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }


            int rowIdx = 3;
            for (StatisticalForQuarterProjections s : list) {
                Row row = sheet.createRow(rowIdx++);
                Cell yearCell = row.createCell(0);
                yearCell.setCellValue(s.getYear());
                yearCell.setCellStyle(dataStyle);

                Cell quarterCell = row.createCell(1);
                quarterCell.setCellValue("Quý " + s.getQuarter());
                quarterCell.setCellStyle(dataStyle);

                Cell revenueCell = row.createCell(2);
                revenueCell.setCellValue(s.getOrderCount());
                revenueCell.setCellStyle(moneyStyle);

                Cell totalOrderCell = row.createCell(3);
                totalOrderCell.setCellValue(s.getOrderCount());
                totalOrderCell.setCellStyle(dataStyle);
            }

            Row totalRow = sheet.createRow(rowIdx);
            Cell totalLabel = totalRow.createCell(1);
            totalLabel.setCellValue("TỔNG CỘNG:");
            CellStyle totalStyle = workbook.createCellStyle();
            totalStyle.cloneStyleFrom(headerStyle);
            totalLabel.setCellStyle(totalStyle);

            double totalRevenue = list.stream()
                    .mapToDouble(StatisticalForQuarterProjections::getQuarter)
                    .sum();
            Cell totalRevenueCell = totalRow.createCell(2);
            totalRevenueCell.setCellValue(totalRevenue);
            totalRevenueCell.setCellStyle(moneyStyle);

            long totalOrders = list.stream()
                    .mapToLong(StatisticalForQuarterProjections::getOrderCount)
                    .sum();
            Cell totalOrderCell = totalRow.createCell(3);
            totalOrderCell.setCellValue(totalOrders);
            totalOrderCell.setCellStyle(dataStyle);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
            workbook.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi xuất Excel: " + e.getMessage());
        }
    }

    @GetMapping("/profit")
    public List<Map<String, Object>> getProfitStatistics(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return orderService.getProfitByDate(startDate, endDate);
    }

    @GetMapping("/statistics/discount")
    public ResponseEntity<List<Map<String, Object>>> getDiscountStatistics() {
        List<Object[]> stats = discountUsageRepository.getDiscountStatistics();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : stats) {
            Map<String, Object> map = new HashMap<>();
            map.put("discountName", row[0]);
            map.put("usageCount", row[1]);
            map.put("totalDiscountedAmount", row[2]);
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/total-revenue")
    public ResponseEntity<?> getTotalRevenue() {
        double total = statisticsService.getTotalRevenue();
        return ResponseEntity.ok(total);
    }
}
