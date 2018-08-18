package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;

import javax.persistence.Entity;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor
@ToString(callSuper=true)
public class ChartOfAccount extends NamedEntity{

	@Builder
	public ChartOfAccount(String id, String name, LocalDateTime created, String user, String desc) {
		super(id, name, created, user, desc);
	}
}
