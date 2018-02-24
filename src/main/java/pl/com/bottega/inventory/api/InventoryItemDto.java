package pl.com.bottega.inventory.api;

public class InventoryItemDto {

	private String skuCode;
	private Integer amount;

	public InventoryItemDto(String skuCode, Integer amount) {
		this.skuCode = skuCode;
		this.amount = amount;
	}

	public String getSkuCode() {
		return skuCode;
	}

	public void setSkuCode(String skuCode) {
		this.skuCode = skuCode;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}
}
