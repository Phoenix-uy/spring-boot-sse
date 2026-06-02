package com.servicenow.poc.sse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorData implements DataModel {
    private String sensorId;
    private Double temperature;
    private Double humidity;
    private String status;
    private Long timestamp;
    
    @Override
    public String getDataType() {
        return "sensor";
    }
}
