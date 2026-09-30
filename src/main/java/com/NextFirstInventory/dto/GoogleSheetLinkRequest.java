package com.NextFirstInventory.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoogleSheetLinkRequest {

    private String name;
    private String url;
}