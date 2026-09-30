package com.NextFirstInventory.service;

import com.NextFirstInventory.dto.GoogleSheetLinkDto;

import java.util.List;

public interface GoogleSheetLinkService {

    List<GoogleSheetLinkDto> findAll();

    GoogleSheetLinkDto add(String name, String url);

    void delete(Long id);
}