package com.example.bully.controller;

import com.example.bully.dto.CoordinatorPayload;
import com.example.bully.dto.ElectionPayload;
import com.example.bully.dto.NodeInfo;
import com.example.bully.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*") // Permite acesso de qualquer origem (Frontend)
public class NodeController {

    @Autowired
    private NodeService nodeService;

    private ResponseEntity<Void> handleRequestWhenDown() {
        if (nodeService.isDown()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return null;
    }

    @PostMapping("/ping")
    public ResponseEntity<Void> ping() {
        ResponseEntity<Void> downResponse = handleRequestWhenDown();
        if (downResponse != null) return downResponse;
        return ResponseEntity.ok().build();
    }

    @PostMapping("/election")
    public ResponseEntity<Void> handleElection(@RequestBody ElectionPayload payload) {
        ResponseEntity<Void> downResponse = handleRequestWhenDown();
        if (downResponse != null) return downResponse;

        try {
            nodeService.handleElectionRequest(payload);
            return ResponseEntity.ok().build();
        } catch (NodeService.ServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    @PostMapping("/coordinator")
    public ResponseEntity<Void> handleCoordinator(@RequestBody CoordinatorPayload payload) {
        ResponseEntity<Void> downResponse = handleRequestWhenDown();
        if (downResponse != null) return downResponse;

        try {
            nodeService.handleCoordinatorAnnouncement(payload);
            return ResponseEntity.ok().build();
        } catch (NodeService.ServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    @GetMapping("/info")
    public ResponseEntity<NodeInfo> getInfo() {
        return ResponseEntity.ok(nodeService.getInfo());
    }

    @PostMapping("/kill")
    public ResponseEntity<String> killNode() {
        nodeService.setDown(true);
        return ResponseEntity.ok("Nó " + nodeService.getInfo().getNodeId() + " está agora simulando uma falha.");
    }

    @PostMapping("/revive")
    public ResponseEntity<String> reviveNode() {
        nodeService.setDown(false);
        return ResponseEntity.ok("Nó " + nodeService.getInfo().getNodeId() + " foi revivido.");
    }

    @PostMapping("/start-election")
    public ResponseEntity<String> startElectionManually() {
        if (nodeService.isDown()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Não é possível iniciar a eleição, o nó está inativo.");
        }
        nodeService.startElection();
        return ResponseEntity.ok("Eleição iniciada manualmente.");
    }
}
