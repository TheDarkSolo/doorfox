package com.doorfox.dto;

import com.doorfox.entity.FunnelStage;

import java.time.Instant;
import java.util.UUID;

public class LeadDtos {

    public record LeadResponse(
            UUID id,
            String name,
            String phone,
            String instagramHandle,
            String budget,
            String projectType,
            String timeline,
            String location,
            FunnelStage funnelStage,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record UpdateLeadRequest(
            String name,
            String budget,
            String projectType,
            String timeline,
            String location,
            FunnelStage funnelStage
    ) {
    }
}
