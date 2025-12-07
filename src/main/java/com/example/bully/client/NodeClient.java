package com.example.bully.client;

import com.example.bully.dto.CoordinatorPayload;
import com.example.bully.dto.ElectionPayload;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;

@FeignClient(name = "node-client", url = "placeholder") // url é um placeholder, pois será dinâmico
public interface NodeClient {

    @PostMapping("/ping")
    ResponseEntity<Void> ping(URI baseUrl);

    @PostMapping("/election")
    ResponseEntity<Void> startElection(URI baseUrl, @RequestBody ElectionPayload payload);

    @PostMapping("/coordinator")
    ResponseEntity<Void> announceCoordinator(URI baseUrl, @RequestBody CoordinatorPayload payload);
}
