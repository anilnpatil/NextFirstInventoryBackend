package com.NextFirstInventory.controller;

import com.NextFirstInventory.dto.InventoryItemDto;
import com.NextFirstInventory.dto.InventorySyncResult;
import com.NextFirstInventory.service.InventoryService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "http://localhost:4200")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<InventoryItemDto> search(@RequestParam(defaultValue = "") String q) {
        return inventoryService.search(q);
    }

    @PostMapping("/sync")
    public InventorySyncResult syncFromGoogleSheets() {
        return inventoryService.syncFromGoogleSheet();
    }
}
