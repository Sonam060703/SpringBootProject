package com.restaurant.reservation.converter;

import com.restaurant.reservation.dto.ReservationResponse;
import com.restaurant.reservation.model.Reservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationConverter {
    public ReservationResponse toDto(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .tableId(reservation.getTable().getId())
                .tableName(reservation.getTable().getName())
                .reservationDateTime(reservation.getReservationDateTime())
                .guestCount(reservation.getGuestCount())
                .specialRequests(reservation.getSpecialRequests())
                .cancelled(reservation.isCancelled())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}