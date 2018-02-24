package pl.com.bottega.inventory.api;

import pl.com.bottega.inventory.domain.commands.Validatable;

public interface Handler<C extends Validatable, R> {
	R handle(C command);
	Class<? extends Validatable> getSupportedCommandClass();
	default boolean canHandle(Validatable command) {
		return command.getClass().equals(getSupportedCommandClass());
	}
}
