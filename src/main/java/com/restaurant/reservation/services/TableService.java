package com.restaurant.reservation.service;

import com.restaurant.reservation.converter.TableConverter;
import com.restaurant.reservation.dto.TableDto;
import com.restaurant.reservation.exception.ResourceNotFoundException;
import com.restaurant.reservation.model.Table;
import com.restaurant.reservation.repository.TableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TableService {
    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private TableConverter tableConverter;

    public List<TableDto> getAllTables() {
        return tableRepository.findAll().stream()
                .map(tableConverter::toDto)
                .collect(Collectors.toList());
    }

    public List<TableDto> getAvailableTables() {
        return tableRepository.findByAvailableTrue().stream()
                .map(tableConverter::toDto)
                .collect(Collectors.toList());
    }

    public List<TableDto> getAvailableTablesForTimeSlot(LocalDateTime startTime, LocalDateTime endTime) {
        return tableRepository.findAvailableTablesForTimeSlot(startTime, endTime).stream()
                .map(tableConverter::toDto)
                .collect(Collectors.toList());
    }

    public TableDto getTableById(Long id) {
        Table table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + id));
        return tableConverter.toDto(table);
    }

    public TableDto createTable(TableDto tableDto) {
        Table table = tableConverter.toEntity(tableDto);
        Table savedTable = tableRepository.save(table);
        return tableConverter.toDto(savedTable);
    }

    public TableDto updateTable(Long id, TableDto tableDto) {
        Table table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + id));

        table.setName(tableDto.getName());
        table.setCapacity(tableDto.getCapacity());
        table.setLocation(tableDto.getLocation());
        table.setAvailable(tableDto.isAvailable());

        Table updatedTable = tableRepository.save(table);
        return tableConverter.toDto(updatedTable);
    }

    public void deleteTable(Long id) {
        Table table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + id));
        tableRepository.delete(table);
    }
}