package com.vnpt.mini_project_java.util;

import com.vnpt.mini_project_java.dto.CategoryDTO;
import com.vnpt.mini_project_java.dto.ProductDTO;
import com.vnpt.mini_project_java.dto.TrademarkDTO;
import com.vnpt.mini_project_java.projections.StatisticalForMonthProjections;
import com.vnpt.mini_project_java.projections.StatisticalForYearProjections;
import com.vnpt.mini_project_java.projections.StatisticalProductProjections;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtil {

    public static List<ProductDTO> readProductsFromExcel(MultipartFile file) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        List<ProductDTO> products = new ArrayList<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            Cell nameCell = row.getCell(0);
            Cell descriptionCell = row.getCell(1);
            Cell dateCell = row.getCell(2);
            Cell priceCell = row.getCell(3);
            Cell categoryIdCell = row.getCell(4);
            Cell categoryNameCell = row.getCell(5);
            Cell tradeIdCell = row.getCell(6);
            Cell tradeNameCell = row.getCell(7);
            Cell imageCell = row.getCell(8);

            if (nameCell != null && descriptionCell != null && dateCell != null && priceCell != null
                    && categoryIdCell != null && categoryNameCell != null && tradeIdCell != null && tradeNameCell != null && imageCell != null) {

                String name = nameCell.getStringCellValue();
                String description = descriptionCell.getStringCellValue();
                LocalDate dateProduct = null;
                if (dateCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dateCell)) {
                    dateProduct = dateCell.getDateCellValue().toInstant().
                            atZone(ZoneId.systemDefault()).toLocalDate();
                } else if (dateCell.getCellType() == CellType.STRING) {
                    String dateStr = dateCell.getStringCellValue();
                    try {
                        dateProduct = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    } catch (DateTimeParseException e) {
                        System.err.println("Invalid date format at row " + row.getRowNum() + ": " + dateStr);
                        continue;
                    }
                }
                double price = priceCell.getNumericCellValue();
                Long categoryId = (long) categoryIdCell.getNumericCellValue();
                String categoryName = categoryNameCell.getStringCellValue();
                Long tradeId = (long) tradeIdCell.getNumericCellValue();
                String tradeName = tradeNameCell.getStringCellValue();
                String imageUrl = "";
                if (imageCell.getCellType() == CellType.STRING) {
                    imageUrl = imageCell.getStringCellValue();
                } else if (imageCell.getCellType() == CellType.BLANK) {
                    imageUrl = "";
                }

                ProductDTO productDTO = new ProductDTO();
                productDTO.setName(name);
                productDTO.setDescription(description);
                productDTO.setDate_product(String.valueOf(dateProduct));
                productDTO.setPrice(price);
                productDTO.setCategoryID(categoryId);
                productDTO.setCategoryname(categoryName);
                productDTO.setTradeID(tradeId);
                productDTO.setTradeName(tradeName);
                productDTO.setImage(imageUrl);

                products.add(productDTO);
            }
        }
        workbook.close();
        return products;
    }

    public static List<CategoryDTO> readCategoryFromExcel(MultipartFile file) throws IOException{
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        List<CategoryDTO> categorys = new ArrayList<>();

        for(Row row : sheet){
            String name = row.getCell(0).getStringCellValue();

            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setName(name);

            categorys.add(categoryDTO);
        }
        workbook.close();
        return categorys;
    }

    public static List<TrademarkDTO> readTrademarkExcel(MultipartFile file) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        List<TrademarkDTO> list = new ArrayList<>();

        for (Row row : sheet) {

            if (row.getRowNum() == 0) continue;

            Cell idCell = row.getCell(0);
            Cell nameCell = row.getCell(1);

            if (idCell == null || nameCell == null) continue;

            TrademarkDTO dto = new TrademarkDTO();
            dto.setTradeID((long) idCell.getNumericCellValue());
            dto.setTradeName(nameCell.getStringCellValue());

            list.add(dto);
        }

        workbook.close();
        return list;
    }

    public static byte[] createStatisticalProductExcel(List<StatisticalProductProjections> statistics) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Statistical Product");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(1).setCellValue("Product Name");
        headerRow.createCell(2).setCellValue("Total Quantity");
        headerRow.createCell(3).setCellValue("Total Revenue");

        int rowNum = 1;
        for (StatisticalProductProjections statistic : statistics) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(1).setCellValue(statistic.getName());
            row.createCell(2).setCellValue(statistic.getQuantitysold());
            row.createCell(3).setCellValue(statistic.getTotal());
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    public static byte[] createStatisticalForYearExcel(List<StatisticalForYearProjections> statis){
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Statistical For Year");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(1).setCellValue("Year");
        headerRow.createCell(2).setCellValue("OrderCount");
        headerRow.createCell(3).setCellValue("Total");
        headerRow.createCell(4).setCellValue("Mintotal");
        headerRow.createCell(5).setCellValue("Maxtotal");

        int rowNum = 1;
        for (StatisticalForYearProjections statistical : statis){
            Row row = sheet.createRow(rowNum++);
            row.createCell(1).setCellValue(statistical.getYear());
            row.createCell(2).setCellValue(statistical.getOrderCount());
            row.createCell(3).setCellValue(statistical.getTotal());
            row.createCell(4).setCellValue(statistical.getMinTotal());
            row.createCell(5).setCellValue(statistical.getMaxTotal());
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  outputStream.toByteArray();
    }

    public static byte[] createStatisticalForMonth(List<StatisticalForMonthProjections> statictical){
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Statistical For Month");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(1).setCellValue("Month");
        headerRow.createCell(2).setCellValue("Year");
        headerRow.createCell(3).setCellValue("OrderCount");
        headerRow.createCell(4).setCellValue("Total");
        headerRow.createCell(5).setCellValue("Mintotal");
        headerRow.createCell(6).setCellValue("Maxtotal");

        int rowNum = 1;

        for (StatisticalForMonthProjections statistical : statictical){
            Row row = sheet.createRow(rowNum++);
            row.createCell(1).setCellValue(statistical.getMonth());
            row.createCell(2).setCellValue(statistical.getYear());
            row.createCell(3).setCellValue(statistical.getOrderCount());
            row.createCell(4).setCellValue(statistical.getTotal());
            row.createCell(5).setCellValue(statistical.getMinTotal());
            row.createCell(6).setCellValue(statistical.getMaxTotal());
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try{
            workbook.write(outputStream);
            workbook.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }
}
