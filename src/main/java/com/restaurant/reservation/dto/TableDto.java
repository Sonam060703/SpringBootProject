package com.restaurant.reservation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TableDto {
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    @Min(1)
    private Integer capacity;

    private String location;
    private boolean available;
}
