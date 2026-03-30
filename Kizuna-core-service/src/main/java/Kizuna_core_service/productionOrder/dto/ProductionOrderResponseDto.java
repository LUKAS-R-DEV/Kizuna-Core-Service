package Kizuna_core_service.productionOrder.dto;

import Kizuna_core_service.productionOrder.domain.ProductionOrderStatus;

import java.time.LocalDateTime;

public record ProductionOrderResponseDto(Long id,String recipeName,Integer quantityToProduce, LocalDateTime startTime, LocalDateTime endTime, ProductionOrderStatus status,Integer estimatedTotalTime) {

}
