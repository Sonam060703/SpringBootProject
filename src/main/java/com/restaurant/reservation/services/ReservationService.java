package com.restaurant.reservation.service;

import com.restaurant.reservation.converter.ReservationConverter;
import com.restaurant.reservation.dto.ReservationRequest;
import com.restaurant.reservation.dto.ReservationResponse;
import com.restaurant.reservation.exception.ResourceNotFoundException;
import com.restaurant.reservation.model.Reservation;
import com.restaurant.reservation.model.Table;
import com.restaurant.reservation.model.User;
import com.restaurant.reservation.repository.ReservationRepository;
import com.restaurant.reservation.repository.TableRepository;
import com.restaurant.reservation.repository.UserRepository;
import com.restaurant.reservation.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationConverter reservationConverter;

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(reservationConverter::toDto)
                .collect(Collectors.toList());
    }

    public List<ReservationResponse> getCurrentUserReservations() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return reservationRepository.findByUser(user).stream()
                .map(reservationConverter::toDto)
                .collect(Collectors.toList());
    }

    public ReservationResponse createReservation(Long tableId, ReservationRequest reservationRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Table table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + tableId));

        if (!table.isAvailable()) {
            throw new RuntimeException("Table is not available");
        }

        LocalDateTime reservationTime = reservationRequest.getReservationDateTime();
        LocalDateTime endTime = reservationTime.plusHours(2); // Assuming 2-hour time slot

        List<Table> availableTables = tableRepository.findAvailableTablesForTimeSlot(reservationTime, endTime);
        if (!availableTables.contains(table)) {
            throw new RuntimeException("Table is already reserved for this time slot");
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .table(table)
                .reservationDateTime(reservationRequest.getReservationDateTime())
                .guestCount(reservationRequest.getGuestCount())
                .specialRequests(reservationRequest.getSpecialRequests())
                .cancelled(false)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);
        return reservationConverter.toDto(savedReservation);
    }

    public ReservationResponse cancelReservation(Long tableId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Reservation reservation = reservationRepository.findByUserAndTableIdAndCancelledFalse(user, tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Active reservation not found for this table"));

        reservation.setCancelled(true);
        Reservation updatedReservation = reservationRepository.save(reservation);

        return reservationConverter.toDto(updatedReservation);
    }
}
