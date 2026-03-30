package Kizuna_core_service.productionOrder.dto;

import jakarta.validation.constraints.NotNull;

public record ProductionOrderRequestDto(
        @NotNull (message = "Recipe id cannot be null")
        Long recipeId ,
        @NotNull (message = "Quantity to produce cannot be null")
        Integer quantityToProduce) {

}
