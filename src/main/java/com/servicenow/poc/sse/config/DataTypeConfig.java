package com.servicenow.poc.sse.config;

import com.servicenow.poc.sse.model.SensorData;
import com.servicenow.poc.sse.model.SimpleData;
import com.servicenow.poc.sse.model.UserActivity;
import com.servicenow.poc.sse.registry.DataTypeRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataTypeConfig {
    
    @Value("${app.data.type:simple}")
    private String defaultDataType;
    
    @Bean
    public DataTypeRegistry dataTypeRegistry() {
        DataTypeRegistry registry = new DataTypeRegistry();
        registry.register("simple", SimpleData.class);
        registry.register("sensor", SensorData.class);
        registry.register("user_activity", UserActivity.class);
        registry.setActiveDataType(defaultDataType);
        log.info("Data types registered. Active type: {}", defaultDataType);
        return registry;
    }
}
