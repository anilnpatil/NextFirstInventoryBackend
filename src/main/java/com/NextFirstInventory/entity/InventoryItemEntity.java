package com.NextFirstInventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_group")
    private String stockGroup;

    @Column(name = "batch_name")
    private String batchName;

    @Column(name = "make")
    private String make;

    @Column(name = "item_name", columnDefinition = "TEXT")
    private String itemName;

    @Column(name = "part_no")
    private String partNumber;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "rate")
    private BigDecimal rate;

    @Column(name = "value")
    private BigDecimal value;

    @Column(name = "rack_no")
    private String rackNumber;
}