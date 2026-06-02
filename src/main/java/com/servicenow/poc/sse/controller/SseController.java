package com.servicenow.poc.sse.controller;

import com.servicenow.poc.sse.model.DataModel;
import com.servicenow.poc.sse.service.DataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseController {
    
    private final DataService dataService;
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<DataModel>> streamData() {
        return dataService.getDataStream()
                .map(data -> ServerSentEvent.<DataModel>builder()
                        .id(String.valueOf(System.currentTimeMillis()))
                        .event("data-update")
                        .data(data)
                        .build())
                .mergeWith(Flux.interval(Duration.ofSeconds(30))
                        .map(seq -> ServerSentEvent.<DataModel>builder()
                                .comment("keep-alive")
                                .build()));
    }
}
