ALTER TABLE inventory_items RENAME COLUMN model_number TO model_no;
ALTER TABLE inventory_items RENAME COLUMN part_number TO part_no;
ALTER TABLE inventory_items RENAME COLUMN rack_number TO rack_no;
ALTER TABLE inventory_items RENAME COLUMN quantity TO stock_qty;
ALTER TABLE inventory_items RENAME COLUMN condition TO stock_status;