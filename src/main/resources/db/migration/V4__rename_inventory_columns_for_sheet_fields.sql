ALTER TABLE inventory_items RENAME COLUMN description TO item_name;
ALTER TABLE inventory_items RENAME COLUMN stock_qty TO quantity;
ALTER TABLE inventory_items RENAME COLUMN item_value TO value;