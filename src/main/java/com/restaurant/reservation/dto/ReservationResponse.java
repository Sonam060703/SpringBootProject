package com.restaurant.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationResponse {
    private Long id;
    private Long tableId;
    private String tableName;
    private LocalDateTime reservationDateTime;
    private int guestCount;
    private String specialRequests;
    private boolean cancelled;
    private LocalDateTime createdAt;
}