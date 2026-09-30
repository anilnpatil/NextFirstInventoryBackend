package com.NextFirstInventory.repository;

import com.NextFirstInventory.entity.GoogleSheetLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoogleSheetLinkRepository extends JpaRepository<GoogleSheetLinkEntity, Long> {

    boolean existsBySpreadsheetIdAndSheetId(String spreadsheetId, Integer sheetId);
}