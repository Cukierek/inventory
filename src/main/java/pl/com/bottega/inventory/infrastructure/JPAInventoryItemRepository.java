package pl.com.bottega.inventory.infrastructure;


import org.springframework.stereotype.Component;
import pl.com.bottega.inventory.domain.GenericJpaRepository;
import pl.com.bottega.inventory.domain.InventoryItem;
import pl.com.bottega.inventory.domain.InventoryItemRepository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Optional;

@Component
public class JPAInventoryItemRepository extends GenericJpaRepository<InventoryItem> implements InventoryItemRepository {
	@Override
	public Optional<InventoryItem> findBySkuCode(String skuCode) {
		try {
			Query query = entityManager.createQuery("FROM InventoryItem i WHERE i.skuCode = :skuCode")
					.setParameter("skuCode", skuCode);
			return Optional.of((InventoryItem) query.getSingleResult());
		} catch (NoResultException ex) {
			return Optional.empty();
		}
	}
}