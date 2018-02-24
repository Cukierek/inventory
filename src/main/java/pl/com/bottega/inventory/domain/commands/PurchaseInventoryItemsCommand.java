package pl.com.bottega.inventory.domain.commands;

import java.util.Map;

public class PurchaseInventoryItemsCommand implements Validatable {

	Map<String, Integer> products;

	public PurchaseInventoryItemsCommand(Map<String, Integer> products) {
		this.products = products;
	}

	public Map<String, Integer> getProducts() {
		return products;
	}

	public void setProducts(Map<String, Integer> products) {
		this.products = products;
	}

	@Override
	public void validate(ValidationErrors errors) {
		validatePresenceOf(products, "products", errors);
		if (products.isEmpty()) {
			errors.add("skus", "are required");
		} else {
			products.entrySet().stream().forEach(pair -> {
				String skuCode = pair.getKey();
				Integer amount = pair.getValue();
				if (amount == null) errors.add("amount","can't be empty");
				else if (amount < 1 || amount > 999) errors.add(skuCode,"must be between 1 and 999");
			});
		}
	}
}
