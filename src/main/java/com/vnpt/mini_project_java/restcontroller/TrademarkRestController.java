package com.vnpt.mini_project_java.restcontroller;

import com.vnpt.mini_project_java.dto.TrademarkDTO;
import com.vnpt.mini_project_java.entity.Trademark;
import com.vnpt.mini_project_java.service.trademark.TrademarkService;
import com.vnpt.mini_project_java.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/trademark", produces = MediaType.APPLICATION_JSON_VALUE)
public class TrademarkRestController {

    @Autowired
    private TrademarkService trademarkService;

    @GetMapping("/getall")
    public ResponseEntity<?> getListtrademarkdto(){
        return ResponseEntity.ok(trademarkService.getAllTrademarkDTO());
    }

    @GetMapping("/gettrademark")
    public ResponseEntity<List<TrademarkDTO>> getList(){
        List<TrademarkDTO> trademarkDTOS = trademarkService.getAllTrademarkDTO();
        return ResponseEntity.ok(trademarkDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrademarkDTO> getTrademarkById(@PathVariable(name = "id") Long id){
        Trademark trademark = trademarkService.getTrademarkById(id);

        TrademarkDTO trademarkResponse = new TrademarkDTO(trademark);

        return ResponseEntity.ok().body(trademarkResponse);
    }

    @PostMapping("/add")
    public ResponseEntity<?> createTrademark(@RequestBody TrademarkDTO dto){
        try{
            TrademarkDTO createdTrademark = trademarkService.saveDTO(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTrademark);
        }catch (IllegalArgumentException ex){
            Map<String, String> error = new HashMap<>();
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }catch (EntityNotFoundException ex){
            Map<String, String> error = new HashMap<>();
            error.put("error", "Không tìm thấy dữ liệu hợp lệ");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<TrademarkDTO> updateTrademark(@PathVariable long id,@RequestBody TrademarkDTO trademarkDTO){
       try{
           Trademark trademark = trademarkService.updateTramemark(id,trademarkDTO);
           TrademarkDTO updateDTO = new TrademarkDTO(trademark);
           System.out.println("Updated Trademark: " + trademark.getTradeName());
           return ResponseEntity.ok(updateDTO);
       }catch (EntityNotFoundException ex){
           System.out.println("Error" + ex.getMessage());
           return ResponseEntity.notFound().build();
       }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteTrademark(@PathVariable long id){
        try{
            trademarkService.deleteTrademarkById(id);
            return ResponseEntity.ok().body("{\"status\":\"success\"}");
        }catch (EntityNotFoundException ex){
            System.out.println("Error" + ex.getMessage());
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadExcelProductTemplate(){
        try{
            InputStream inputStream = getClass().getResourceAsStream("/templates/API-TRADEMARK.xlsx");
            InputStreamResource resource = new InputStreamResource(inputStream);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=API-TRADEMARK.xlsx");
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/import-trademark")
    public ResponseEntity<Map<String, String>> importTrademark(@RequestParam("file") MultipartFile file) {
        Map<String, String> res = new HashMap<>();

        try {
            List<TrademarkDTO> list = ExcelUtil.readTrademarkExcel(file);

            if (list.isEmpty()) {
                res.put("message", "File không có dữ liệu thương hiệu!");
                return ResponseEntity.badRequest().body(res);
            }

            trademarkService.importTrademarkExcel(list);

            res.put("message", "Import thương hiệu thành công!");
            return ResponseEntity.ok(res);

        } catch (RuntimeException e) {
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);

        } catch (Exception e) {
            res.put("message", "Lỗi import: " + e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }
}
