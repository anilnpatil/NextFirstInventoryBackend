package com.NextFirstInventory.dto;

import java.util.List;

public record InventorySearchResponse(
        List<InventoryItemDto> content,
        long totalElements,
        int totalPages,
        int page,
        int size) {
}