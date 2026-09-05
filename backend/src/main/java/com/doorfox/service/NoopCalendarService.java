package com.doorfox.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
public class NoopCalendarService implements CalendarService {

    @Override
    public String bookOfficeVisit(UUID leadId, Instant scheduledAt) {
        log.info("[no-op] Google Calendar not configured — would book office visit for lead {} at {}", leadId, scheduledAt);
        return null;
    }
}
