package com.NextFirstInventory.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
public class InventoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    @Column(name = "model_no")
    private String modelNo;
    @Column(name = "part_no")
    private String partNo;
    private String make;
    @Column(name = "rack_no")
    private String rackNo;
    @Column(name = "stock_qty")
    private Integer stockQty;
    @Column(name = "stock_status")
    private String stockStatus;
    private String location;
}
