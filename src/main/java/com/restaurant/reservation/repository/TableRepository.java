package com.restaurant.reservation.repository;

import com.restaurant.reservation.model.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TableRepository extends JpaRepository<Table, Long> {
    List<Table> findByAvailableTrue();

    @Query("""
        SELECT t FROM Table t
        WHERE t.available = true
        AND t.id NOT IN (
            SELECT r.table.id FROM Reservation r
            WHERE r.reservationDateTime BETWEEN :startTime AND :endTime
            AND r.cancelled = false
        )
    """)
    List<Table> findAvailableTablesForTimeSlot(LocalDateTime startTime, LocalDateTime endTime);
}