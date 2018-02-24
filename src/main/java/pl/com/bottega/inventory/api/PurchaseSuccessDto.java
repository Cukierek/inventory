package pl.com.bottega.inventory.api;

import java.util.HashMap;
import java.util.Map;

public class PurchaseSuccessDto extends PurchaseDto {
	private Map<String, Integer> purchasedProducts;

	public PurchaseSuccessDto() {
		setSuccess(true);
		this.purchasedProducts = new HashMap<>();
	}

	public Map<String, Integer> getPurchasedProducts() {
		return purchasedProducts;
	}

	public void setPurchasedProducts(Map<String, Integer> purchasedProducts) {
		this.purchasedProducts = purchasedProducts;
	}
}
