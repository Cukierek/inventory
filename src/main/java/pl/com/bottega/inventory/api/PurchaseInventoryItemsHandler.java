package pl.com.bottega.inventory.api;

import org.springframework.stereotype.Component;
import pl.com.bottega.inventory.domain.InventoryItem;
import pl.com.bottega.inventory.domain.InventoryItemRepository;
import pl.com.bottega.inventory.domain.commands.InvalidCommandException;
import pl.com.bottega.inventory.domain.commands.PurchaseInventoryItemsCommand;
import pl.com.bottega.inventory.domain.commands.Validatable;

import javax.transaction.Transactional;
import java.util.Optional;

@Component
public class PurchaseInventoryItemsHandler implements Handler<PurchaseInventoryItemsCommand, PurchaseDto> {

	private InventoryItemRepository inventoryItemRepository;

	public PurchaseInventoryItemsHandler(InventoryItemRepository inventoryItemRepository) {
		this.inventoryItemRepository = inventoryItemRepository;
	}

	@Override
	@Transactional
	public PurchaseDto handle(PurchaseInventoryItemsCommand command) {
		Validatable.ValidationErrors validationErrors = new Validatable.ValidationErrors();
		PurchaseDto purchaseDto;
		PurchaseSuccessDto successDto = new PurchaseSuccessDto();
		PurchaseFailedDto failedDto = new PurchaseFailedDto();
		command.getProducts().entrySet().stream().forEach(pair -> {
			String requestedSkuCode = pair.getKey();
			Integer requestedAmount = pair.getValue();
			Optional<InventoryItem> inventoryItem = inventoryItemRepository.findBySkuCode(requestedSkuCode);
			if (isPresentInRepository(inventoryItem)) {
				InventoryItem item = inventoryItem.get();
				if (item.getAmount() < requestedAmount) {
					failedDto.getMissingProducts().put(requestedSkuCode, requestedAmount);
				} else if (item.getAmount() >= requestedAmount) {
					successDto.getPurchasedProducts().put(requestedSkuCode, requestedAmount);
				}
			} else {
				validationErrors.add(requestedSkuCode, "no such sku");
			}
		});
		if (failedDto.getMissingProducts().isEmpty() && validationErrors.isValid()) {
			purchaseDto = successDto;
			persistChanges(command);
		} else {
			purchaseDto = failedDto;
		}
		if (!validationErrors.isValid()) throw new InvalidCommandException(validationErrors);
		return purchaseDto;
	}

	private void persistChanges(PurchaseInventoryItemsCommand command) {
		command.getProducts().entrySet().stream().forEach(pair -> {
			String requestedSkuCode = pair.getKey();
			Integer requestedAmount = pair.getValue();
			Optional<InventoryItem> inventoryItem = inventoryItemRepository.findBySkuCode(requestedSkuCode);
			if (isPresentInRepository(inventoryItem)) {
				InventoryItem item = inventoryItem.get();
				item.decreaseStock(requestedAmount);
				inventoryItemRepository.save(item);
			}
		});
	}

	@Override
	public Class<? extends Validatable> getSupportedCommandClass() {
		return PurchaseInventoryItemsCommand.class;
	}

	private boolean isPresentInRepository(Optional<InventoryItem> inventoryItem) {
		return inventoryItem.isPresent();
	}
}
