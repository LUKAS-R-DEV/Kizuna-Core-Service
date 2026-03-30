package Kizuna_core_service.productionOrder.controller;

import Kizuna_core_service.productionOrder.domain.ProductionOrderStatus;
import Kizuna_core_service.productionOrder.dto.ProductionOrderRequestDto;
import Kizuna_core_service.productionOrder.dto.ProductionOrderResponseDto;
import Kizuna_core_service.productionOrder.service.productionOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/productionOrders")
public class ProductionOrderController {
    private final productionOrderService productionOrderService;
    public ProductionOrderController(productionOrderService productionOrderService) {
        this.productionOrderService = productionOrderService;
    }
    @GetMapping
    public List<ProductionOrderResponseDto> findAll(){
        return productionOrderService.findAll();
    }
    @GetMapping("/{status}")
    public List<ProductionOrderResponseDto> findByStatus(ProductionOrderStatus status){
        return productionOrderService.findByStatus(status);
    }
    @PostMapping
    public ResponseEntity<ProductionOrderResponseDto> create(ProductionOrderRequestDto requestDto){
        ProductionOrderResponseDto productionOrderResponseDto = productionOrderService.create(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(productionOrderResponseDto);
    }
    @PostMapping("/{id}/start")
    public ResponseEntity<ProductionOrderResponseDto> start(Long id){
        ProductionOrderResponseDto productionOrderResponseDto = productionOrderService.start(id);
        return ResponseEntity.ok(productionOrderResponseDto);
    }
    @PostMapping("/{id}/finish")
    public ResponseEntity<ProductionOrderResponseDto> finish(Long id){
        ProductionOrderResponseDto productionOrderResponseDto = productionOrderService.finish(id);
        return ResponseEntity.ok(productionOrderResponseDto);
    }
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ProductionOrderResponseDto> cancel(Long id){
        ProductionOrderResponseDto productionOrderResponseDto = productionOrderService.cancel(id);
        return ResponseEntity.ok(productionOrderResponseDto);
    }
}
