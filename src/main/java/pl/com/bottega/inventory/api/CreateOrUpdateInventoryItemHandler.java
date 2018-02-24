package pl.com.bottega.inventory.api;

import org.springframework.stereotype.Component;
import pl.com.bottega.inventory.domain.InventoryItem;
import pl.com.bottega.inventory.domain.InventoryItemRepository;
import pl.com.bottega.inventory.domain.commands.CreateOrUpdateInventoryItemCommand;
import pl.com.bottega.inventory.domain.commands.Validatable;

import javax.transaction.Transactional;
import java.util.Optional;

@Component
public class CreateOrUpdateInventoryItemHandler implements Handler<CreateOrUpdateInventoryItemCommand, Void> {

	private InventoryItemRepository inventoryItemRepository;

	public CreateOrUpdateInventoryItemHandler(InventoryItemRepository inventoryItemRepository) {
		this.inventoryItemRepository = inventoryItemRepository;
	}

	@Override
	@Transactional
	public Void handle(CreateOrUpdateInventoryItemCommand command) {
		Optional<InventoryItem> inventoryItem = inventoryItemRepository.findBySkuCode(command.getSkuCode());
		if (isPresentInRepository(inventoryItem)) {
			updateExistingInventoryItem(command, inventoryItem);
		} else {
			createNewInventoryItem(command);
		}
		return null;
	}

	private void createNewInventoryItem(CreateOrUpdateInventoryItemCommand command) {
		InventoryItem newInventoryItem = new InventoryItem(command);
		inventoryItemRepository.save(newInventoryItem);
	}

	private void updateExistingInventoryItem(CreateOrUpdateInventoryItemCommand command, Optional<InventoryItem> inventoryItem) {
		InventoryItem existingInventoryItem = inventoryItem.get();
		existingInventoryItem.increaseStock(command.getAmount());
		inventoryItemRepository.save(existingInventoryItem);
	}

	@Override
	public Class<? extends Validatable> getSupportedCommandClass() {
		return CreateOrUpdateInventoryItemCommand.class;
	}

	private boolean isPresentInRepository(Optional<InventoryItem> inventoryItem) {
		return inventoryItem.isPresent();
	}
}
