package com.NextFirstInventory.repository;

import com.NextFirstInventory.entity.InventoryItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InventoryRepository extends JpaRepository<InventoryItemEntity, Long>,
	JpaSpecificationExecutor<InventoryItemEntity> {
}
