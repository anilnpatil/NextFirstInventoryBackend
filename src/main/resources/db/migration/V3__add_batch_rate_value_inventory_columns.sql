ALTER TABLE inventory_items ADD COLUMN batch_name VARCHAR(255);
ALTER TABLE inventory_items ADD COLUMN rate NUMERIC(19, 4);
ALTER TABLE inventory_items ADD COLUMN item_value NUMERIC(19, 4);