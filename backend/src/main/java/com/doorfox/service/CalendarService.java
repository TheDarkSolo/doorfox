package com.doorfox.service;

import java.time.Instant;
import java.util.UUID;

/**
 * TODO: wire up real Google Calendar OAuth (a Google Cloud project + consent
 * screen is required — see .env.example for GOOGLE_CALENDAR_* vars). Until
 * then this interface exists so LeadController/BookingRepository callers don't
 * need to change when the real implementation lands.
 */
public interface CalendarService {

    /** Creates a calendar event for an office visit and returns the Google event id, or null if not configured. */
    String bookOfficeVisit(UUID leadId, Instant scheduledAt);
}
