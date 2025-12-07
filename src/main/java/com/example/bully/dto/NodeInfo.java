package com.example.bully.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeInfo {
    private int nodeId;
    private int leaderId;
    private boolean isLeader;
    private boolean isAlive;
    private String message;
}
