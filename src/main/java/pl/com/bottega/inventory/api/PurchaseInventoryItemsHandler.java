package pl.com.bottega.inventory.api;

import org.springframework.stereotype.Component;
import pl.com.bottega.inventory.domain.InventoryItem;
import pl.com.bottega.inventory.domain.InventoryItemRepository;
import pl.com.bottega.inventory.domain.commands.InvalidCommandException;
import pl.com.bottega.inventory.domain.commands.PurchaseInventoryItemsCommand;
import pl.com.bottega.inventory.domain.commands.Validatable;

import javax.transaction.Transactional;
import java.util.HashMap;
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
		Map<String, InventoryItem> localInventoryMap = new HashMap<>();
		PurchaseDto purchaseDto;
		PurchaseSuccessDto successDto = new PurchaseSuccessDto();
		PurchaseFailedDto failedDto = new PurchaseFailedDto();
		command.getProducts().entrySet().stream().forEach(pair -> {
			processPair(validationErrors, successDto, failedDto, pair, localInventoryMap);
		});
		if (failedDto.getMissingProducts().isEmpty() && validationErrors.isValid()) {
			purchaseDto = successDto;
			persistChanges(command, localInventoryMap);
		} else {
			purchaseDto = failedDto;
		}
		if (!validationErrors.isValid()) throw new InvalidCommandException(validationErrors);
		return purchaseDto;
	}

	private void processPair(Validatable.ValidationErrors validationErrors, PurchaseSuccessDto successDto,
	                         PurchaseFailedDto failedDto, Map.Entry<String, Integer> pair,
	                         Map<String, InventoryItem> localInventoryMap) {
		String requestedSkuCode = pair.getKey();
		Integer requestedAmount = pair.getValue();
		Optional<InventoryItem> inventoryItem = inventoryItemRepository.findBySkuCode(requestedSkuCode);
		validateForPresence(validationErrors, successDto, failedDto, requestedSkuCode,
				requestedAmount, inventoryItem, localInventoryMap);
	}

	private void validateForPresence(Validatable.ValidationErrors validationErrors, PurchaseSuccessDto successDto,
	                                 PurchaseFailedDto failedDto, String requestedSkuCode, Integer requestedAmount,
	                                 Optional<InventoryItem> inventoryItem,
	                                 Map<String, InventoryItem> localInventoryMap) {
		if (isPresentInRepository(inventoryItem)) {
			localInventoryMap.put(requestedSkuCode, inventoryItem.get());
			validateStock(successDto, failedDto, requestedSkuCode, requestedAmount, inventoryItem);
		} else {
			validationErrors.add(requestedSkuCode, "no such sku");
		}
	}

	private void validateStock(PurchaseSuccessDto successDto, PurchaseFailedDto failedDto,
	                           String requestedSkuCode, Integer requestedAmount, Optional<InventoryItem> inventoryItem) {
		InventoryItem item = inventoryItem.get();
		if (item.getAmount() < requestedAmount) {
			failedDto.getMissingProducts().put(requestedSkuCode, requestedAmount);
		} else if (item.getAmount() >= requestedAmount) {
			successDto.getPurchasedProducts().put(requestedSkuCode, requestedAmount);
		}
	}

	private void persistChanges(PurchaseInventoryItemsCommand command, Map<String, InventoryItem> localInventoryMap) {
		command.getProducts().entrySet().stream().forEach(pair -> {
			String requestedSkuCode = pair.getKey();
			Integer requestedAmount = pair.getValue();
			InventoryItem item = localInventoryMap.get(requestedSkuCode);
			item.decreaseStock(requestedAmount);
			inventoryItemRepository.save(item);
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
