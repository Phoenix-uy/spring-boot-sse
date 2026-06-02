package com.servicenow.poc.sse.controller;

import com.servicenow.poc.sse.model.DataModel;
import com.servicenow.poc.sse.service.DataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataController {
    
    private final DataService dataService;
    
    @GetMapping
    public ResponseEntity<DataModel> getData() {
        return ResponseEntity.ok(dataService.getData());
    }
    
    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getInfo() {
        return ResponseEntity.ok(Map.of(
            "dataType", dataService.getCurrentDataType(),
            "modelClass", dataService.getData().getClass().getSimpleName()
        ));
    }

    @PostMapping
    public ResponseEntity<DataModel> updateData(@RequestBody Map<String, Object> rawData) {
        DataModel newData = dataService.convertAndUpdate(rawData);
        return ResponseEntity.ok(newData);
    }
}
