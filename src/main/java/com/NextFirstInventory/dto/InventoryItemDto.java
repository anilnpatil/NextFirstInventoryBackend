package com.NextFirstInventory.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemDto {

    private Long id;
    private String stockGroup;
    private String make;
    private String batchName;
    private String itemName;
    private String partNumber;
    private Integer quantity;
    private BigDecimal rate;
    private BigDecimal value;
    private String rackNumber;
}
