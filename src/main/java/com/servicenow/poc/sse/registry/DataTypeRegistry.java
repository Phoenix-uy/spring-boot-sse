package com.servicenow.poc.sse.registry;

import com.servicenow.poc.sse.model.DataModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DataTypeRegistry {
    
    private final Map<String, Class<? extends DataModel>> registry = new ConcurrentHashMap<>();
    private String activeDataType;
    
    public void register(String dataType, Class<? extends DataModel> modelClass) {
        registry.put(dataType, modelClass);
        log.info("Registered data type: {} -> {}", dataType, modelClass.getSimpleName());
    }
    
    public void setActiveDataType(String dataType) {
        if (!registry.containsKey(dataType)) {
            throw new IllegalArgumentException("Data type not registered: " + dataType);
        }
        this.activeDataType = dataType;
        log.info("Active data type set to: {}", dataType);
    }
    
    public String getActiveDataType() {
        return activeDataType;
    }
    
    public Class<? extends DataModel> getActiveModelClass() {
        if (activeDataType == null) {
            throw new IllegalStateException("No active data type set");
        }
        return registry.get(activeDataType);
    }
    
    public Class<? extends DataModel> getModelClass(String dataType) {
        return registry.get(dataType);
    }
    
    public boolean isRegistered(String dataType) {
        return registry.containsKey(dataType);
    }
    
    public Map<String, Class<? extends DataModel>> getAllRegisteredTypes() {
        return Map.copyOf(registry);
    }
}
