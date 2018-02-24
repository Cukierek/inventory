package pl.com.bottega.inventory.api;

import java.util.HashMap;
import java.util.Map;

public class PurchaseFailedDto extends PurchaseDto {
	private Map<String, Integer> missingProducts;

	public PurchaseFailedDto() {
		setSuccess(false);
		this.missingProducts = new HashMap<>();
	}

	public Map<String, Integer> getMissingProducts() {
		return missingProducts;
	}

	public void setMissingProducts(Map<String, Integer> missingProducts) {
		this.missingProducts = missingProducts;
	}
}
