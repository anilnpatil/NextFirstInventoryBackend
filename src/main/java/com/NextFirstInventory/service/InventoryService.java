package com.NextFirstInventory.service;

import com.NextFirstInventory.dto.InventoryItemDto;
import com.NextFirstInventory.dto.InventorySyncResult;

import java.util.List;

public interface InventoryService {

    List<InventoryItemDto> search(String query);

    InventorySyncResult syncFromGoogleSheet();
}
