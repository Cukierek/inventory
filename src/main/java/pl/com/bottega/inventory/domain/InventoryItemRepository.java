package pl.com.bottega.inventory.domain;

import java.util.Optional;

public interface InventoryItemRepository extends Repository<InventoryItem> {
	Optional<InventoryItem> findBySkuCode(String skuCode);
}