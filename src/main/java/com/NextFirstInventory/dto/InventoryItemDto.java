package com.NextFirstInventory.dto;

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
    private String description;
    private String modelNumber;
    private String partNumber;
    private String make;
    private String rackNumber;
    private Integer quantity;
    private String condition;
    private String location;
}
