package com.NextFirstInventory.controller;

import com.NextFirstInventory.dto.GoogleSheetLinkDto;
import com.NextFirstInventory.dto.GoogleSheetLinkRequest;
import com.NextFirstInventory.service.GoogleSheetLinkService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/google-sheets")
@CrossOrigin(origins = "http://localhost:4200")
public class GoogleSheetLinkController {

    private final GoogleSheetLinkService service;

    public GoogleSheetLinkController(GoogleSheetLinkService service) {
        this.service = service;
    }

    @GetMapping
    public List<GoogleSheetLinkDto> findAll() {
        return service.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GoogleSheetLinkDto add(@RequestBody GoogleSheetLinkRequest request) {
        return service.add(request.getName(), request.getUrl());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}