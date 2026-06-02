package com.servicenow.poc.sse.config;

import com.servicenow.poc.sse.model.SensorData;
import com.servicenow.poc.sse.model.SimpleData;
import com.servicenow.poc.sse.model.UserActivity;
import com.servicenow.poc.sse.registry.DataTypeRegistry;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataTypeConfig {
    
    private final DataTypeRegistry registry;
    
    @Value("${app.data.type:simple}")
    private String defaultDataType;
    
    @PostConstruct
    public void registerDataTypes() {
        registry.register("simple", SimpleData.class);
        registry.register("sensor", SensorData.class);
        registry.register("user_activity", UserActivity.class);
        
        registry.setActiveDataType(defaultDataType);
        log.info("Data types registered. Active type: {}", defaultDataType);
    }
}
