package com.NextFirstInventory.service.serviceImpl;

import com.NextFirstInventory.entity.InventoryItemEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryServiceImplTest {

    private final InventoryServiceImpl service = new InventoryServiceImpl(null, null);

    @Test
    void mapsHeadersUsedBySavedSheetTabs() {
        InventoryItemEntity firstTabItem = service.parseCsv(
                "\"SL NO\",\"Description\",\"Model\",\"Part No\",\"Make\",\"Stock Qty\",\"Rack NO A\",\"Stock Status\"\n"
                        + "\"1\",\"Silencer\",\"U-1/4\",\"2316\",\"FESTO\",\"1\",\"A1\",\"Available\"",
                "Tab A").get(0);
        InventoryItemEntity secondTabItem = service.parseCsv(
                "\"Sl No\",\"Make\",\"Description\",\"Part No.\",\"Model No.\",\"Stock Qty\",\"Rack No B\",\"Stock Status\"\n"
                        + "\"2\",\"SUNX\",\"SENSOR\",\"KT6W-P5116\",\"1027500\",\"3\",\"B13\",\"Available\"",
                "Tab B").get(0);
        InventoryItemEntity thirdTabItem = service.parseCsv(
                "\"Sl\",\"Make\",\"Description\",\"Model No\",\"Part No\",\"Stock Qty\",\"Rack\",\"Stock Status\"\n"
                        + "\"1\",\"ALLEN BRADLEY\",\"PLC\",\"1762-L24BWA\",\"1762-L24BWA\",\"3\",\"B2\",\"Available\"",
                "Tab C").get(0);

        assertEquals("Silencer", firstTabItem.getItemName());
        assertEquals("Tab A", firstTabItem.getStockGroup());
        assertEquals("2316", firstTabItem.getPartNumber());
        assertEquals(1, firstTabItem.getQuantity());
        assertEquals("A1", firstTabItem.getRackNumber());

        assertEquals("SENSOR", secondTabItem.getItemName());
        assertEquals("KT6W-P5116", secondTabItem.getPartNumber());
        assertEquals("B13", secondTabItem.getRackNumber());

        assertEquals("PLC", thirdTabItem.getItemName());
        assertEquals("1762-L24BWA", thirdTabItem.getPartNumber());
        assertEquals("B2", thirdTabItem.getRackNumber());
    }

        @Test
        void mapsNewInventorySheetColumns() {
                InventoryItemEntity item = service.parseCsv(
                                "\"Make\",\"Batch Name\",\"Item Name\",\"Part No.\",\"Quantity\",\"Rate\",\"Value\",\"Rack No\"\n"
                                                + "\"FESTO\",\"B-001\",\"Pneumatic Valve\",\"2316\",\"5\",\"12.50\",\"62.50\",\"A1\"",
                                "Festo Pneumatic").get(0);

                assertEquals("FESTO", item.getMake());
                assertEquals("Festo Pneumatic", item.getStockGroup());
                assertEquals("B-001", item.getBatchName());
                assertEquals("Pneumatic Valve", item.getItemName());
                assertEquals("2316", item.getPartNumber());
                assertEquals(5, item.getQuantity());
                assertEquals(new BigDecimal("12.50"), item.getRate());
                assertEquals(new BigDecimal("62.50"), item.getValue());
                assertEquals("A1", item.getRackNumber());
        }
}