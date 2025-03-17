package com.restaurant.reservation.controller;

import com.restaurant.reservation.dto.ReservationRequest;
import com.restaurant.reservation.dto.ReservationResponse;
import com.restaurant.reservation.dto.TableDto;
import com.restaurant.reservation.service.ReservationService;
import com.restaurant.reservation.service.TableService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/tables")
@PreAuthorize("hasRole('USER')")
public class UserController {
    @Autowired
    private TableService tableService;

    @Autowired
    private ReservationService reservationService;

    @GetMapping
    public ResponseEntity<List<TableDto>> getAvailableTables(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime datetime) {
        List<TableDto> tables;

        if (datetime != null) {
            LocalDateTime endTime = datetime.plusHours(2); // Assuming 2-hour slots
            tables = tableService.getAvailableTablesForTimeSlot(datetime, endTime);
        } else {
            tables = tableService.getAvailableTables();
        }

        return ResponseEntity.ok(tables);
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<ReservationResponse> reserveTable(
            @PathVariable(value = "id") Long tableId,
            @Valid @RequestBody ReservationRequest reservationRequest) {
        ReservationResponse reservation = reservationService.createReservation(tableId, reservationRequest);
        return ResponseEntity.ok(reservation);
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable(value = "id") Long tableId) {
        ReservationResponse reservation = reservationService.cancelReservation(tableId);
        return ResponseEntity.ok(reservation);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ReservationResponse>> getReservationHistory() {
        List<ReservationResponse> reservations = reservationService.getCurrentUserReservations();
        return ResponseEntity.ok(reservations);
    }
}
