package Kizuna_core_service.productionOrder.domain;

import Kizuna_core_service.recipe.domain.Recipe;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "production_order")
@Data
public class ProductionOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer quantityToProduce;
    private LocalDateTime startTime;
    private Long estimatedTotalTime;
    private LocalDateTime endTime;
    private Boolean inspection;
    @Enumerated(EnumType.STRING)
    private ProductionOrderStatus status;
    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;


}
