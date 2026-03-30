package Kizuna_core_service.productionOrder.service;

import Kizuna_core_service.inventory.domain.Inventory;
import Kizuna_core_service.inventory.domain.Status;
import Kizuna_core_service.inventory.repository.InventoryRepository;
import Kizuna_core_service.inventory_movement.domain.InventoryMovement;
import Kizuna_core_service.inventory_movement.domain.MovementType;
import Kizuna_core_service.inventory_movement.repository.InventoryMovementRepository;
import Kizuna_core_service.productionOrder.domain.ProductionOrder;
import Kizuna_core_service.productionOrder.domain.ProductionOrderStatus;
import Kizuna_core_service.productionOrder.dto.ProductionOrderRequestDto;
import Kizuna_core_service.productionOrder.dto.ProductionOrderResponseDto;
import Kizuna_core_service.productionOrder.repository.ProductionOrderRepository;
import Kizuna_core_service.recipe.domain.Recipe;
import Kizuna_core_service.recipe.domain.RecipeItem;
import Kizuna_core_service.recipe.repository.RecipeRepository;
import Kizuna_core_service.shared.exception.BusinessException;
import Kizuna_core_service.shared.exception.NotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service

public class ProductionOrderService {
    private final ProductionOrderRepository productionOrderRepository;
    private final RecipeRepository recipeRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public ProductionOrderService(ProductionOrderRepository productionOrderRepository, RecipeRepository recipeRepository, InventoryRepository inventoryRepository, InventoryMovementRepository inventoryMovementRepository) {
        this.productionOrderRepository = productionOrderRepository;
        this.recipeRepository = recipeRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    public List<ProductionOrderResponseDto> findAll(){
        return productionOrderRepository.findAll().stream().map(this::productionOrderResponseDto).toList();
    }
    public ProductionOrderResponseDto findById(Long id){
        ProductionOrder productionOrder=productionOrderRepository.findById(id).orElseThrow(() -> new NotFoundException("Production order not found"));
        return productionOrderResponseDto(productionOrder);
    }
    public List<ProductionOrderResponseDto> findByStatus(ProductionOrderStatus status){
        return productionOrderRepository.findByStatus(status).stream().map(this::productionOrderResponseDto).toList();
    }

    @Transactional
    public ProductionOrderResponseDto create(ProductionOrderRequestDto requestDto) {
        Recipe recipe = recipeRepository.findById(requestDto.recipeId()).orElseThrow(() -> new NotFoundException("Recipe not found"));
        ProductionOrder productionOrder = new ProductionOrder();
        productionOrder.setQuantityToProduce(requestDto.quantityToProduce());
        productionOrder.setStatus(ProductionOrderStatus.PLANNED);
        productionOrder.setRecipe(recipe);
        productionOrder.setQualityInspectionPassed(false);
        productionOrder.setEstimatedTotalTime(productionOrder.getQuantityToProduce()*recipe.getEstimatedProductionTime());
        productionOrderRepository.save(productionOrder);
        return productionOrderResponseDto(productionOrder);
    }
    @Transactional
    public ProductionOrderResponseDto start(Long id){
        ProductionOrder productionOrder=productionOrderRepository.findById(id).orElseThrow(()-> new NotFoundException("Production order not found"));
        if(!productionOrder.getStatus().equals(ProductionOrderStatus.PLANNED)){
            throw new BusinessException("Production order is not planned");
        }
        Recipe recipe = productionOrder.getRecipe();
        if(recipe.getItems().isEmpty()){
            throw new BusinessException("Recipe has no items");
        }
        List<Inventory> inventoriesToUpdate = new ArrayList<>();
        List<InventoryMovement> movements=new ArrayList<>();
        for(RecipeItem item:recipe.getItems()){
            Inventory inventory = item.getInventory();
            Double consumption = item.getQuantity() * productionOrder.getQuantityToProduce();
            if(inventory.getQuantity()<consumption){
                throw new BusinessException("Not enough inventory for item: " + inventory.getName());
            }
        }
        for(RecipeItem item:recipe.getItems()){
            Inventory inventory = item.getInventory();
            Double consumption = item.getQuantity() * productionOrder.getQuantityToProduce();
            inventory.setQuantity(inventory.getQuantity() - consumption);
            Status status=(inventory.getMinStock()>inventory.getQuantity()) ? Status.CRITICAL : Status.GOOD;

            inventory.setStatus(status);
            String reason="Production order ID: " + productionOrder.getId();
            InventoryMovement inventoryMovement = new InventoryMovement(inventory, consumption, reason, MovementType.EXIT);
            movements.add(inventoryMovement);
            inventoriesToUpdate.add(inventory);
        }
        productionOrder.setStatus(ProductionOrderStatus.IN_PROGRESS);
        productionOrder.setStartTime(LocalDateTime.now());
        inventoryRepository.saveAll(inventoriesToUpdate);
        inventoryMovementRepository.saveAll(movements);
        productionOrderRepository.save(productionOrder);
        return productionOrderResponseDto(productionOrder);
    }
    @Transactional
    public ProductionOrderResponseDto finish(Long id){
        ProductionOrder productionOrder=productionOrderRepository.findById(id).orElseThrow(() -> new NotFoundException("Production order not found"));
        if(!productionOrder.getStatus().equals(ProductionOrderStatus.IN_PROGRESS)){
            throw new BusinessException("Production order is not in progress");
        }
        productionOrder.setStatus(ProductionOrderStatus.COMPLETED);
        productionOrder.setEndTime(LocalDateTime.now());
        productionOrderRepository.save(productionOrder);
        return productionOrderResponseDto(productionOrder);
    }
    public ProductionOrderResponseDto cancel(Long id){
        ProductionOrder productionOrder=productionOrderRepository.findById(id).orElseThrow(()-> new NotFoundException("Production order not found"));
        if(productionOrder.getStatus().equals(ProductionOrderStatus.COMPLETED)){
            throw new BusinessException("Production order is already completed");
        }
        productionOrder.setStatus(ProductionOrderStatus.CANCELLED);
        productionOrderRepository.save(productionOrder);
        return productionOrderResponseDto(productionOrder);
    }
    public Long calculateRemainingTime(ProductionOrder order){
        if(order.getStartTime()==null){
          return order.getEstimatedTotalTime();
        }
        long elapsed = Duration.between(order.getStartTime(), LocalDateTime.now()).toMinutes();
         long remaing=order.getEstimatedTotalTime() - elapsed;
         return Math.max(0, remaing);

    }
    public Double calculateProgress(ProductionOrder order) {
        if (order.getStartTime() == null) {
            return 0.0;
        }
        long total = order.getEstimatedTotalTime();
        long elapsed = Duration.between(order.getStartTime(), LocalDateTime.now()).toMinutes();
        double progress = ((double) elapsed / total) * 100;
        return Math.min(progress, 100.0);
    }

    public LocalDateTime calculateETA(ProductionOrder order) {
        if (order.getStartTime() == null) {
            return null;
        }
        return order.getStartTime().plusMinutes(order.getEstimatedTotalTime());
    }

    public ProductionOrderStatus calculateStatus(ProductionOrder order) {
        if (order.getStartTime() == null) {
            return ProductionOrderStatus.PLANNED;
        }
        long elapsed = Duration.between(order.getStartTime(), LocalDateTime.now()).toMinutes();

        if (elapsed >= order.getEstimatedTotalTime()) {
            return ProductionOrderStatus.COMPLETED;
        }
        return ProductionOrderStatus.IN_PROGRESS;
    }

    private ProductionOrderResponseDto productionOrderResponseDto(ProductionOrder productionOrder){
        return new ProductionOrderResponseDto(productionOrder.getId(),productionOrder.getRecipe().getName(),productionOrder.getQuantityToProduce(),productionOrder.getStartTime(),productionOrder.getQualityInspectionPassed(),productionOrder.getEndTime(),productionOrder.getStatus(),productionOrder.getEstimatedTotalTime(),calculateProgress(productionOrder),calculateETA(productionOrder),calculateRemainingTime(productionOrder),calculateStatus(productionOrder));
    }

}
