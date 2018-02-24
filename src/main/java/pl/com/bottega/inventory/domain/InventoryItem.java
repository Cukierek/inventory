package pl.com.bottega.inventory.domain;

import pl.com.bottega.inventory.domain.commands.CreateOrUpdateInventoryItemCommand;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class InventoryItem {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String skuCode;
	private Integer amount;

	public InventoryItem() {
	}

	public InventoryItem(CreateOrUpdateInventoryItemCommand cmd) {
		this.skuCode = cmd.getSkuCode();
		this.amount = cmd.getAmount();
	}

	public Long getId() {
		return id;
	}

	public String getSkuCode() {
		return skuCode;
	}

	public Integer getAmount() {
		return amount;
	}

	public void increaseStock(Integer amount) {
		this.amount += amount;
	}

	public void decreaseStock(Integer amount) {
		this.amount -= amount;
	}
}