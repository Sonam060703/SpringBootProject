package com.restaurant.reservation;

import com.restaurant.reservation.converter.TableConverter;
import com.restaurant.reservation.dto.TableDto;
import com.restaurant.reservation.exception.ResourceNotFoundException;
import com.restaurant.reservation.model.Table;
import com.restaurant.reservation.repository.TableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TableServiceTest {
    @Mock
    private TableRepository tableRepository;

    @Mock
    private TableConverter tableConverter;

    @InjectMocks
    private TableServiceTest tableService;

    private Table table;
    private TableDto tableDto;

    @BeforeEach
    void setUp() {
        table = new Table(1L, "Table 1", 4, "Corner", true);
        tableDto = new TableDto(1L, "Table 1", 4, "Corner", true);
    }

    @Test
    void testGetAllTables() {
        when(tableRepository.findAll()).thenReturn(Arrays.asList(table));
        when(tableConverter.toDto(table)).thenReturn(tableDto);

        List<TableDto> result = tableService.getAllTables();

        assertEquals(1, result.size());
        assertEquals("Table 1", result.get(0).getName());
    }

    @Test
    void testGetAvailableTables() {
        when(tableRepository.findByAvailableTrue()).thenReturn(Arrays.asList(table));
        when(tableConverter.toDto(table)).thenReturn(tableDto);

        List<TableDto> result = tableService.getAvailableTables();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isAvailable());
    }

    @Test
    void testGetAvailableTablesForTimeSlot() {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);
        when(tableRepository.findAvailableTablesForTimeSlot(startTime, endTime)).thenReturn(Arrays.asList(table));
        when(tableConverter.toDto(table)).thenReturn(tableDto);

        List<TableDto> result = tableService.getAvailableTablesForTimeSlot(startTime, endTime);

        assertEquals(1, result.size());
    }

    @Test
    void testGetTableById() {
        when(tableRepository.findById(1L)).thenReturn(Optional.of(table));
        when(tableConverter.toDto(table)).thenReturn(tableDto);

        TableDto result = tableService.getTableById(1L);

        assertEquals("Table 1", result.getName());
    }

    @Test
    void testGetTableById_NotFound() {
        when(tableRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tableService.getTableById(2L));
    }

    @Test
    void testCreateTable() {
        when(tableConverter.toEntity(tableDto)).thenReturn(table);
        when(tableRepository.save(table)).thenReturn(table);
        when(tableConverter.toDto(table)).thenReturn(tableDto);

        TableDto result = tableService.createTable(tableDto);

        assertEquals("Table 1", result.getName());
    }

    @Test
    void testUpdateTable() {
        when(tableRepository.findById(1L)).thenReturn(Optional.of(table));
        when(tableRepository.save(any(Table.class))).thenReturn(table);
        when(tableConverter.toDto(table)).thenReturn(tableDto);

        TableDto result = tableService.updateTable(1L, tableDto);

        assertEquals("Table 1", result.getName());
    }

    @Test
    void testUpdateTable_NotFound() {
        when(tableRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tableService.updateTable(2L, tableDto));
    }

    @Test
    void testDeleteTable() {
        when(tableRepository.findById(1L)).thenReturn(Optional.of(table));
        doNothing().when(tableRepository).delete(table);

        assertDoesNotThrow(() -> tableService.deleteTable(1L));
    }

    @Test
    void testDeleteTable_NotFound() {
        when(tableRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tableService.deleteTable(2L));
    }
}
