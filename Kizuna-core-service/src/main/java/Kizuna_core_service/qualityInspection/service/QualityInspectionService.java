package Kizuna_core_service.qualityInspection.service;

import Kizuna_core_service.inventory.domain.Inventory;
import Kizuna_core_service.inventory.domain.Status;
import Kizuna_core_service.inventory.repository.InventoryRepository;
import Kizuna_core_service.inventory_movement.domain.InventoryMovement;
import Kizuna_core_service.inventory_movement.domain.MovementType;
import Kizuna_core_service.inventory_movement.repository.InventoryMovementRepository;
import Kizuna_core_service.productionOrder.domain.ProductionOrder;
import Kizuna_core_service.productionOrder.domain.ProductionOrderStatus;
import Kizuna_core_service.productionOrder.repository.ProductionOrderRepository;
import Kizuna_core_service.qualityInspection.domain.QualityInspection;
import Kizuna_core_service.qualityInspection.domain.QualityInspectionStatus;
import Kizuna_core_service.qualityInspection.dto.QualityInspectionRequestDto;
import Kizuna_core_service.qualityInspection.dto.QualityInspectionResponseDto;
import Kizuna_core_service.qualityInspection.repository.QualityInspectionRepository;
import Kizuna_core_service.shared.exception.BusinessException;
import Kizuna_core_service.shared.exception.NotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QualityInspectionService {
    private final QualityInspectionRepository qualityInspectionRepository;
    private final ProductionOrderRepository productionOrderRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public QualityInspectionService(QualityInspectionRepository qualityInspectionRepository, ProductionOrderRepository productionOrderRepository, InventoryRepository inventoryRepository, InventoryMovementRepository inventoryMovementRepository) {
        this.qualityInspectionRepository = qualityInspectionRepository;
        this.productionOrderRepository = productionOrderRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    public List<QualityInspectionResponseDto> findAll(){
        return qualityInspectionRepository.findAll().stream().map(this::qualityInspectionResponseDto).toList();
    }
    public QualityInspectionResponseDto findById(Long id){
        QualityInspection qualityInspection = qualityInspectionRepository.findById(id).orElseThrow(() -> new NotFoundException("Quality inspection not found"));
        return qualityInspectionResponseDto(qualityInspection);
    }

    @Transactional
    public QualityInspectionResponseDto create(QualityInspectionRequestDto qualityInspectionRequestDto){
        ProductionOrder productionOrder=productionOrderRepository.findById(qualityInspectionRequestDto.productionOrderId()).orElseThrow(()-> new NotFoundException("Quality inspection not found"));

        if(!productionOrder.getStatus().equals(ProductionOrderStatus.COMPLETED)){
            throw new BusinessException("Production order must be completed to create quality inspection");
        }
        if(productionOrder.getInspection()==true){
            throw new BusinessException("Production order already passed quality inspection");
        }
        QualityInspection inspection=new QualityInspection();
        inspection.setInspectedBy(qualityInspectionRequestDto.inspectedBy());
        inspection.setNotes(qualityInspectionRequestDto.notes());
        inspection.setStatus(qualityInspectionRequestDto.status());
        inspection.setProductionOrder(productionOrder);
        inspection.setCreatedAt(LocalDateTime.now());

        if(inspection.getStatus().equals(QualityInspectionStatus.REJECTED)){
            qualityInspectionRepository.save(inspection);
            productionOrderRepository.save(productionOrder);
            productionOrder.setInspection(true);
            return qualityInspectionResponseDto(inspection);
        }
        productionOrderRepository.save(productionOrder);
        Inventory inventory=productionOrder.getRecipe().getProduct();
        Double quantityProduced=productionOrder.getQuantityToProduce().doubleValue();
        String reason="Quality inspection passed successfully ID: "+inspection.getId();
        productionOrder.setInspection(true);
        InventoryMovement inventoryMovement=new InventoryMovement(inventory,quantityProduced,reason,MovementType.ENTRY);
        inventory.setQuantity(inventory.getQuantity()+quantityProduced);
        Status status=(inventory.getMinStock()>inventory.getQuantity()) ? Status.CRITICAL : Status.GOOD;
        inventory.setStatus(status);

        inventoryMovementRepository.save(inventoryMovement);
        inventoryRepository.save(inventory);
        qualityInspectionRepository.save(inspection);
        return qualityInspectionResponseDto(inspection);
    }



    private QualityInspectionResponseDto qualityInspectionResponseDto(QualityInspection qualityInspection){
        return new QualityInspectionResponseDto(qualityInspection.getId(),qualityInspection.getProductionOrder().getRecipe().getName(),qualityInspection.getStatus(),qualityInspection.getNotes(),qualityInspection.getInspectedBy(),qualityInspection.getCreatedAt());
    }





}
