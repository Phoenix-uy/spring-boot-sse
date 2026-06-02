package com.servicenow.poc.sse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicenow.poc.sse.model.DataModel;
import com.servicenow.poc.sse.registry.DataTypeRegistry;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataService {
    
    private final DataTypeRegistry registry;
    private final AtomicReference<DataModel> dataStore = new AtomicReference<>();
    private final Sinks.Many<DataModel> sink = Sinks.many().multicast().onBackpressureBuffer();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private WatchService watchService;
    private ExecutorService executorService;
    
    @Value("${app.data.file:data.json}")
    private String dataFilePath;
    
    @PostConstruct
    public void init() {
        loadDataFromFile();
        startFileWatcher();
    }
    
    @PreDestroy
    public void cleanup() {
        try {
            if (watchService != null) {
                watchService.close();
            }
            if (executorService != null) {
                executorService.shutdown();
            }
        } catch (IOException e) {
            log.error("Error closing watch service", e);
        }
    }
    
    private void loadDataFromFile() {
        try {
            Path path = Paths.get(dataFilePath);
            Class<? extends DataModel> modelClass = registry.getActiveModelClass();
            
            if (!Files.exists(path)) {
                DataModel defaultData = createDefaultData(modelClass);
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), defaultData);
                log.info("Created default data file: {} with type: {}", dataFilePath, modelClass.getSimpleName());
            }
            
            DataModel data = objectMapper.readValue(path.toFile(), modelClass);
            dataStore.set(data);
            log.info("Loaded data from file: {} (type: {})", dataFilePath, data.getDataType());
        } catch (IOException e) {
            log.error("Error loading data from file", e);
        }
    }
    
    private DataModel createDefaultData(Class<? extends DataModel> modelClass) {
        try {
            return modelClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create default instance of " + modelClass.getSimpleName(), e);
        }
    }
    
    private void startFileWatcher() {
        try {
            Path path = Paths.get(dataFilePath).toAbsolutePath();
            Path directory = path.getParent();
            
            watchService = FileSystems.getDefault().newWatchService();
            directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            
            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> {
                log.info("File watcher started for: {}", path);
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        WatchKey key = watchService.take();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            Path changed = (Path) event.context();
                            if (changed.toString().equals(path.getFileName().toString())) {
                                Thread.sleep(100);
                                loadDataFromFile();
                                notifyClients();
                            }
                        }
                        key.reset();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        } catch (IOException e) {
            log.error("Error starting file watcher", e);
        }
    }
    
    private void notifyClients() {
        DataModel currentData = dataStore.get();
        if (currentData != null) {
            sink.tryEmitNext(currentData);
            log.info("File changed. Notified {} subscribers (type: {})", 
                    sink.currentSubscriberCount(), currentData.getDataType());
        }
    }
    
    public DataModel getData() {
        return dataStore.get();
    }
    
    public Flux<DataModel> getDataStream() {
        return sink.asFlux()
                .doOnSubscribe(sub -> log.info("New SSE client connected"))
                .doOnCancel(() -> log.info("SSE client disconnected"));
    }
    
    public String getCurrentDataType() {
        return registry.getActiveDataType();
    }
    
    public void updateData(DataModel newData) {
        try {
            Path path = Paths.get(dataFilePath);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), newData);
            dataStore.set(newData);
            notifyClients();
            log.info("Data updated via API: {}", newData.getDataType());
        } catch (IOException e) {
            log.error("Error updating data file", e);
            throw new RuntimeException("Failed to update data", e);
        }
    }
}
