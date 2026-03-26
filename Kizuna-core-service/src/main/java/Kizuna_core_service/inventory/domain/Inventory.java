package Kizuna_core_service.inventory.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "inventory")
@Data
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private String location;
    private Double quantity;
    private Double minStock;
    private String supplier;
    @Enumerated(EnumType.STRING)
    private Status status;
    private Boolean active=true;


}