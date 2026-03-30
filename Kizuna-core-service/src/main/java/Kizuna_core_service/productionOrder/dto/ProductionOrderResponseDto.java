package Kizuna_core_service.productionOrder.dto;

import Kizuna_core_service.productionOrder.domain.ProductionOrderStatus;

import java.time.LocalDateTime;

public record ProductionOrderResponseDto(Long id,String recipeName,Integer quantityToProduce, LocalDateTime startTime,Boolean inspection ,LocalDateTime endTime, ProductionOrderStatus status,Long estimatedTotalTime,Double progress,LocalDateTime eta,Long remainingTime,ProductionOrderStatus calculatedStatus) {

}
