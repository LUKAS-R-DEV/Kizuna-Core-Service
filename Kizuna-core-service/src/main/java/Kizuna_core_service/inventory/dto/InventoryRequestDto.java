package Kizuna_core_service.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record InventoryRequestDto(
        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "category is required")
        String category,
        @NotBlank(message = "location is required")
        String location,
        @NotNull(message = "quantity is required")
        @PositiveOrZero(message = "quantity must be positive or zero")
        Double quantity,
        @NotNull(message = "minStock is required")
        @PositiveOrZero(message = "minStock must be positive or zero")
        Double minStock,
        @NotNull(message = "supplier is required")
        String supplier ) {
}
