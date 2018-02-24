package pl.com.bottega.inventory.api;

import org.springframework.stereotype.Component;
import pl.com.bottega.inventory.domain.InventoryItem;
import pl.com.bottega.inventory.domain.InventoryItemRepository;
import pl.com.bottega.inventory.domain.commands.InvalidCommandException;
import pl.com.bottega.inventory.domain.commands.PurchaseInventoryItemsCommand;
import pl.com.bottega.inventory.domain.commands.Validatable;

import javax.transaction.Transactional;
import java.util.Map;
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
			processPair(validationErrors, successDto, failedDto, pair);
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

	private void processPair(Validatable.ValidationErrors validationErrors, PurchaseSuccessDto successDto, PurchaseFailedDto failedDto, Map.Entry<String, Integer> pair) {
		String requestedSkuCode = pair.getKey();
		Integer requestedAmount = pair.getValue();
		Optional<InventoryItem> inventoryItem = inventoryItemRepository.findBySkuCode(requestedSkuCode);
		validateForPresence(validationErrors, successDto, failedDto, requestedSkuCode, requestedAmount, inventoryItem);
	}

	private void validateForPresence(Validatable.ValidationErrors validationErrors, PurchaseSuccessDto successDto, PurchaseFailedDto failedDto, String requestedSkuCode, Integer requestedAmount, Optional<InventoryItem> inventoryItem) {
		if (isPresentInRepository(inventoryItem)) {
			validateStock(successDto, failedDto, requestedSkuCode, requestedAmount, inventoryItem);
		} else {
			validationErrors.add(requestedSkuCode, "no such sku");
		}
	}

	private void validateStock(PurchaseSuccessDto successDto, PurchaseFailedDto failedDto, String requestedSkuCode, Integer requestedAmount, Optional<InventoryItem> inventoryItem) {
		InventoryItem item = inventoryItem.get();
		if (item.getAmount() < requestedAmount) {
			failedDto.getMissingProducts().put(requestedSkuCode, requestedAmount);
		} else if (item.getAmount() >= requestedAmount) {
			successDto.getPurchasedProducts().put(requestedSkuCode, requestedAmount);
		}
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
