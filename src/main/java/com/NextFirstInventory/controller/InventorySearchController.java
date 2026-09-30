package com.NextFirstInventory.controller;

import com.NextFirstInventory.dto.InventorySearchRequest;
import com.NextFirstInventory.dto.InventorySearchResponse;
import com.NextFirstInventory.service.InventorySearchService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory/search")
@CrossOrigin(origins = "http://localhost:4200")
public class InventorySearchController {

    private final InventorySearchService inventorySearchService;

    public InventorySearchController(InventorySearchService inventorySearchService) {
        this.inventorySearchService = inventorySearchService;
    }

    @PostMapping
    public InventorySearchResponse search(@RequestBody InventorySearchRequest request) {
        return inventorySearchService.search(request);
    }
}