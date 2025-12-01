package com.vnpt.mini_project_java.restcontroller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value ="/api/logs", produces = MediaType.APPLICATION_JSON_VALUE)
public class LogRestController {

    @GetMapping("/read")
    public ResponseEntity<?> readLogs(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        try {
            String logFilePath = "D:/log/to/logfile.log";
            File file = new File(logFilePath);

            if (!file.exists()) {
                return ResponseEntity.status(404)
                        .body(Collections.singletonMap("error", "Log file not found"));
            }

            List<String> allLines = Files.readAllLines(Paths.get(logFilePath));

            int totalLines = allLines.size();
            int startIndex = Math.max(0, totalLines - (page + 1) * size);
            int endIndex = Math.max(0, totalLines - page * size);

            List<String> pageLines = allLines.subList(startIndex, endIndex);
            Collections.reverse(pageLines);

            Map<String, Object> response = new HashMap<>();
            response.put("logs", pageLines);
            response.put("totalLines", totalLines);
            response.put("page", page);
            response.put("size", size);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/clear")
    public ResponseEntity<?> clearLogs() {
        try {
            String logFilePath = "D:/log/to/logfile.log";
            Files.write(Paths.get(logFilePath), new byte[0]);
            return ResponseEntity.ok("Logs cleared");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
