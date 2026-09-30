package com.NextFirstInventory.dto;

import java.util.Map;

public record InventorySearchRequest(
        String query,
        Map<String, String> filters,
        Integer page,
        Integer size,
        String sortBy,
        String sortDirection) {
}