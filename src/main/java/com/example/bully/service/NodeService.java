package com.example.bully.service;

import com.example.bully.client.NodeClient;
import com.example.bully.dto.CoordinatorPayload;
import com.example.bully.dto.ElectionPayload;
import com.example.bully.dto.NodeInfo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NodeService {

    @Value("${NODE_ID}")
    private int nodeId;

    @Value("${ALL_NODES}")
    private String allNodesStr;

    @Autowired
    private NodeClient nodeClient;

    private Map<Integer, String> allNodes;
    private final AtomicInteger leaderId = new AtomicInteger(-1);
    private final AtomicBoolean electionInProgress = new AtomicBoolean(false);
    private final AtomicBoolean isDown = new AtomicBoolean(false); // Flag para "soft kill"

    @PostConstruct
    public void init() {
        allNodes = new ConcurrentHashMap<>();
        Arrays.stream(allNodesStr.split(","))
                .forEach(nodeStr -> {
                    String[] parts = nodeStr.split(":");
                    allNodes.put(Integer.parseInt(parts[0]), "http://" + parts[1] + ":8080");
                });

        log.info("Node {} inicializado. Todos os nós: {}", nodeId, allNodes);
        forceLeaderCheck();
    }

    public void forceLeaderCheck() {
        int maxId = allNodes.keySet().stream().max(Comparator.naturalOrder()).orElse(nodeId);
        if (nodeId == maxId) {
            becomeLeader();
        } else {
            leaderId.set(maxId);
            checkLeaderHealth(); // Verifica a saúde do líder inicial
        }
    }

    @Scheduled(fixedRate = 2000)
    public void checkLeaderHealth() {
        if (isDown.get() || isLeader()) {
            return; // Para o heartbeat se estiver "morto" ou se for o próprio líder
        }

        int currentLeaderId = leaderId.get();
        if (currentLeaderId == -1) {
            log.warn("Nenhum líder conhecido. Iniciando eleição.");
            startElection();
            return;
        }

        String leaderUrl = allNodes.get(currentLeaderId);
        if (leaderUrl == null) {
            log.error("URL do líder {} não encontrada. Iniciando eleição.", currentLeaderId);
            startElection();
            return;
        }

        try {
            log.debug("Pingando líder {}", currentLeaderId);
            nodeClient.ping(URI.create(leaderUrl));
        } catch (Exception e) {
            log.warn("Líder {} não respondeu. Iniciando eleição.", currentLeaderId);
            leaderId.set(-1);
            startElection();
        }
    }

    public void startElection() {
        if (isDown.get() || !electionInProgress.compareAndSet(false, true)) {
            log.info("Eleição já em andamento ou nó está inativo.");
            return;
        }

        log.info("Node {} iniciando uma eleição.", nodeId);

        Map<Integer, String> higherNodes = allNodes.entrySet().stream()
                .filter(entry -> entry.getKey() > nodeId)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (higherNodes.isEmpty()) {
            becomeLeader();
            return;
        }

        boolean someoneResponded = false;
        for (Map.Entry<Integer, String> entry : higherNodes.entrySet()) {
            try {
                log.info("Enviando pedido de eleição para o nó {}", entry.getKey());
                nodeClient.startElection(URI.create(entry.getValue()), new ElectionPayload(nodeId));
                someoneResponded = true;
            } catch (Exception e) {
                log.warn("Nó {} não respondeu ao pedido de eleição.", entry.getKey());
            }
        }

        if (!someoneResponded) {
            becomeLeader();
        }
        // Se alguém respondeu, aguarda o anúncio do novo líder.
        electionInProgress.set(false);
    }

    public void handleElectionRequest(ElectionPayload payload) {
        if (isDown.get()) {
            throw new ServiceUnavailableException("Nó está simulando uma falha.");
        }
        if (payload.getCandidateId() < nodeId) {
            log.info("Recebida eleição do nó {}, que tem ID menor. Respondendo OK e iniciando minha própria eleição.", payload.getCandidateId());
            startElection();
        } else {
            log.info("Recebida eleição do nó {}, que tem ID maior. Apenas aguardando.", payload.getCandidateId());
        }
    }

    public void handleCoordinatorAnnouncement(CoordinatorPayload payload) {
        if (isDown.get()) {
            throw new ServiceUnavailableException("Nó está simulando uma falha.");
        }
        int newLeaderId = payload.getLeaderId();
        log.info("Recebido anúncio: O novo líder é o nó {}", newLeaderId);
        leaderId.set(newLeaderId);
        electionInProgress.set(false);
    }



    private void becomeLeader() {
        if (isDown.get()) return;

        log.info("Node {} se tornando o novo líder.", nodeId);
        leaderId.set(nodeId);
        electionInProgress.set(false);

        CoordinatorPayload announcement = new CoordinatorPayload(nodeId);
        allNodes.entrySet().stream()
                .filter(entry -> entry.getKey() != nodeId)
                .forEach(entry -> {
                    try {
                        log.info("Anunciando liderança para o nó {}", entry.getKey());
                        nodeClient.announceCoordinator(URI.create(entry.getValue()), announcement);
                    } catch (Exception e) {
                        log.warn("Falha ao anunciar liderança para o nó {}: {}", entry.getKey(), e.getMessage());
                    }
                });
    }

    public NodeInfo getInfo() {
        return new NodeInfo(nodeId, leaderId.get(), isLeader(), !isDown.get(), "Node " + nodeId);
    }

    public boolean isLeader() {
        return nodeId == leaderId.get();
    }

    public void setDown(boolean down) {
        isDown.set(down);
        log.info("Node {} agora está com o estado isDown = {}", nodeId, down);
        if (!down) {
            // Ao "reviver", força uma verificação de saúde ou eleição
            forceLeaderCheck();
        }
    }

    public boolean isDown() {
        return isDown.get();
    }

    // Exceção customizada para clareza
    public static class ServiceUnavailableException extends RuntimeException {
        public ServiceUnavailableException(String message) {
            super(message);
        }
    }
}
