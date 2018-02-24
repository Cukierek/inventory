package pl.com.bottega.inventory.ui;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.com.bottega.inventory.api.CommandGateway;
import pl.com.bottega.inventory.api.PurchaseDto;
import pl.com.bottega.inventory.domain.commands.CreateOrUpdateInventoryItemCommand;
import pl.com.bottega.inventory.domain.commands.PurchaseInventoryItemsCommand;

import java.util.Map;

@RestController
public class InventoryController {

	private CommandGateway gateway;

	public InventoryController(CommandGateway gateway) {
		this.gateway = gateway;
	}

	@PostMapping("/inventory")
	public void createOrUpdate(@RequestBody CreateOrUpdateInventoryItemCommand cmd) {
		gateway.execute(cmd);
	}

	@PostMapping("/purchase")
	public PurchaseDto purchaseItems(@RequestBody Map<String, Integer> values) {
		PurchaseInventoryItemsCommand cmd = new PurchaseInventoryItemsCommand(values);
		return gateway.execute(cmd);
	}
}
