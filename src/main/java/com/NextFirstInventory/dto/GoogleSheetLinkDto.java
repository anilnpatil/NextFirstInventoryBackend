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
public class GoogleSheetLinkDto {

    private Long id;
    private String name;
    private String url;
    private String spreadsheetId;
    private Integer sheetId;
}