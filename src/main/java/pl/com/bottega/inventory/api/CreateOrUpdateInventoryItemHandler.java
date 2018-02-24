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
			InventoryItem existingInventoryItem = inventoryItem.get();
			existingInventoryItem.increaseStock(command.getAmount());
			inventoryItemRepository.save(existingInventoryItem);
		} else {
			InventoryItem newInventoryItem = new InventoryItem(command);
			inventoryItemRepository.save(newInventoryItem);
		}
		return null;
	}

	@Override
	public Class<? extends Validatable> getSupportedCommandClass() {
		return CreateOrUpdateInventoryItemCommand.class;
	}

	private boolean isPresentInRepository(Optional<InventoryItem> inventoryItem) {
		return inventoryItem.isPresent();
	}
}
