package com.NextFirstInventory.service;

import com.NextFirstInventory.dto.InventorySearchRequest;
import com.NextFirstInventory.dto.InventorySearchResponse;

public interface InventorySearchService {

    InventorySearchResponse search(InventorySearchRequest request);
}