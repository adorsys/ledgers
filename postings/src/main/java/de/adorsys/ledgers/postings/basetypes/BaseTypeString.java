package de.adorsys.ledgers.postings.basetypes;

import java.io.Serializable;
import java.lang.reflect.Type;

public class BaseTypeString implements Serializable, Type {

	private static final long serialVersionUID = 185761717531388393L;

	private String value;

	protected BaseTypeString() {}

	protected BaseTypeString(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{\'" + value + "\'}";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		BaseTypeString that = (BaseTypeString) o;

		return value != null ? value.equals(that.value) : that.value == null;

	}

	@Override
	public int hashCode() {
		return value != null ? value.hashCode() : 0;
	}
}
