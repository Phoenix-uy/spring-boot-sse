package com.servicenow.poc.sse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActivity implements DataModel {
    private String userId;
    private String action;
    private List<String> tags;
    private Long timestamp;
    
    @Override
    public String getDataType() {
        return "user_activity";
    }
}
