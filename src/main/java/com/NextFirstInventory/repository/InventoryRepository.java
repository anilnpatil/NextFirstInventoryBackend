package com.NextFirstInventory.repository;

import com.NextFirstInventory.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InventoryRepository extends JpaRepository<InventoryEntity, Long>,
	JpaSpecificationExecutor<InventoryEntity> {
}
