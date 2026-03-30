package Kizuna_core_service.productionOrder.dto;

import Kizuna_core_service.productionOrder.domain.ProductionOrderStatus;
import jakarta.validation.constraints.NotNull;

public record ProductionOrderRequestDto(
        @NotNull (message = "Recipe id cannot be null")
        Long recipeId ,
        @NotNull (message = "Quantity to produce cannot be null")
        Integer quantityToProduce,
        @NotNull (message = "Status cannot be null")
        ProductionOrderStatus status) {

}
