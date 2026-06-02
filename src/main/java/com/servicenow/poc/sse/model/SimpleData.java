package com.servicenow.poc.sse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleData implements DataModel {
    private String message;
    private Integer counter;
    private Long timestamp;
    
    @Override
    public String getDataType() {
        return "simple";
    }
}
