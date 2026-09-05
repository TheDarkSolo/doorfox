package com.doorfox.controller;

import com.doorfox.dto.LeadDtos.LeadResponse;
import com.doorfox.dto.LeadDtos.UpdateLeadRequest;
import com.doorfox.entity.Lead;
import com.doorfox.repository.LeadRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadRepository leadRepository;

    public LeadController(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    @GetMapping
    public List<LeadResponse> list() {
        return leadRepository.findAll().stream().map(this::toResponse).toList();
    }

    @PatchMapping("/{id}")
    public LeadResponse update(@PathVariable UUID id, @RequestBody UpdateLeadRequest request) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead not found"));

        if (request.name() != null) lead.setName(request.name());
        if (request.budget() != null) lead.setBudget(request.budget());
        if (request.projectType() != null) lead.setProjectType(request.projectType());
        if (request.timeline() != null) lead.setTimeline(request.timeline());
        if (request.location() != null) lead.setLocation(request.location());
        if (request.funnelStage() != null) lead.setFunnelStage(request.funnelStage());

        return toResponse(leadRepository.save(lead));
    }

    private LeadResponse toResponse(Lead lead) {
        return new LeadResponse(
                lead.getId(),
                lead.getName(),
                lead.getPhone(),
                lead.getInstagramHandle(),
                lead.getBudget(),
                lead.getProjectType(),
                lead.getTimeline(),
                lead.getLocation(),
                lead.getFunnelStage(),
                lead.getCreatedAt(),
                lead.getUpdatedAt()
        );
    }
}
