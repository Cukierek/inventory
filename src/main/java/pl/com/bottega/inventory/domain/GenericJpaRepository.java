package pl.com.bottega.inventory.domain;

import pl.com.bottega.inventory.infrastructure.NoSuchEntityException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.ParameterizedType;

public abstract class GenericJpaRepository<Aggregate> implements Repository<Aggregate> {

	@PersistenceContext
	protected EntityManager entityManager;

	private Class<Aggregate> aggregateClass;

	public GenericJpaRepository() {
		this.aggregateClass = ((Class<Aggregate>) ((ParameterizedType) getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0]);
	}

	@Override
	public void save(Aggregate o) {
		entityManager.persist(o);
	}

	@Override
	public Aggregate get(Long id) {
		Aggregate a = entityManager.find(aggregateClass, id);
		if (a == null)
			throw new NoSuchEntityException();
		return a;
	}
}