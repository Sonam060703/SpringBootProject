package com.restaurant.reservation.converter;

import com.restaurant.reservation.dto.TableDto;
import com.restaurant.reservation.model.Table;
import org.springframework.stereotype.Component;

@Component
public class TableConverter {
    public TableDto toDto(Table table) {
        return TableDto.builder()
                .id(table.getId())
                .name(table.getName())
                .capacity(table.getCapacity())
                .location(table.getLocation())
                .available(table.isAvailable())
                .build();
    }

    public Table toEntity(TableDto tableDto) {
        return Table.builder()
                .id(tableDto.getId())
                .name(tableDto.getName())
                .capacity(tableDto.getCapacity())
                .location(tableDto.getLocation())
                .available(tableDto.isAvailable())
                .build();
    }
}