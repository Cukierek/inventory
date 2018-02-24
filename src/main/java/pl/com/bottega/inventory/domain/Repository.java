package pl.com.bottega.inventory.domain;

public interface Repository<T> {
	void save(T t);
	T get(Long id);
}