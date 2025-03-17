package com.restaurant.reservation.controller;

import com.restaurant.reservation.dto.ReservationResponse;
import com.restaurant.reservation.dto.TableDto;
import com.restaurant.reservation.service.ReservationService;
import com.restaurant.reservation.service.TableService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Autowired
    private TableService tableService;

    @Autowired
    private ReservationService reservationService;

    @PostMapping("/tables")
    public ResponseEntity<TableDto> createTable(@Valid @RequestBody TableDto tableDto) {
        TableDto createdTable = tableService.createTable(tableDto);
        return ResponseEntity.ok(createdTable);
    }

    @PutMapping("/tables/{id}")
    public ResponseEntity<TableDto> updateTable(
            @PathVariable(value = "id") Long tableId,
            @Valid @RequestBody TableDto tableDto) {
        TableDto updatedTable = tableService.updateTable(tableId, tableDto);
        return ResponseEntity.ok(updatedTable);
    }

    @DeleteMapping("/tables/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable(value = "id") Long tableId) {
        tableService.deleteTable(tableId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tables")
    public ResponseEntity<List<TableDto>> getAllTables() {
        List<TableDto> tables = tableService.getAllTables();
        return ResponseEntity.ok(tables);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        List<ReservationResponse> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(reservations);
    }
}