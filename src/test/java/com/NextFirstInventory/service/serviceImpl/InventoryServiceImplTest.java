package com.NextFirstInventory.service.serviceImpl;

import com.NextFirstInventory.entity.InventoryEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryServiceImplTest {

    private final InventoryServiceImpl service = new InventoryServiceImpl(null, null);

    @Test
    void mapsHeadersUsedBySavedSheetTabs() {
        InventoryEntity firstTabItem = service.parseCsv(
                "\"SL NO\",\"Description\",\"Model\",\"Part No\",\"Make\",\"Stock Qty\",\"Rack NO A\",\"Stock Status\"\n"
                        + "\"1\",\"Silencer\",\"U-1/4\",\"2316\",\"FESTO\",\"1\",\"A1\",\"Available\"",
                "Tab A").get(0);
        InventoryEntity secondTabItem = service.parseCsv(
                "\"Sl No\",\"Make\",\"Description\",\"Part No.\",\"Model No.\",\"Stock Qty\",\"Rack No B\",\"Stock Status\"\n"
                        + "\"2\",\"SUNX\",\"SENSOR\",\"KT6W-P5116\",\"1027500\",\"3\",\"B13\",\"Available\"",
                "Tab B").get(0);
        InventoryEntity thirdTabItem = service.parseCsv(
                "\"Sl\",\"Make\",\"Description\",\"Model No\",\"Part No\",\"Stock Qty\",\"Rack\",\"Stock Status\"\n"
                        + "\"1\",\"ALLEN BRADLEY\",\"PLC\",\"1762-L24BWA\",\"1762-L24BWA\",\"3\",\"B2\",\"Available\"",
                "Tab C").get(0);

        assertEquals("U-1/4", firstTabItem.getModelNo());
        assertEquals("2316", firstTabItem.getPartNo());
        assertEquals(1, firstTabItem.getStockQty());
        assertEquals("A1", firstTabItem.getRackNo());
        assertEquals("Available", firstTabItem.getStockStatus());

        assertEquals("1027500", secondTabItem.getModelNo());
        assertEquals("KT6W-P5116", secondTabItem.getPartNo());
        assertEquals("B13", secondTabItem.getRackNo());

        assertEquals("1762-L24BWA", thirdTabItem.getModelNo());
        assertEquals("1762-L24BWA", thirdTabItem.getPartNo());
        assertEquals("B2", thirdTabItem.getRackNo());
    }
}