package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * All accounts used by a company are defined in a chart of account.
 * 
 * @author fpo
 *
 */
@Entity
@NoArgsConstructor
@ToString(callSuper = true)
/* The name property of a chart of account must be unique. */
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "ChartOfAccount_name_unique") })
public class ChartOfAccount extends NamedEntity {

	@Builder
	public ChartOfAccount(String id, LocalDateTime created, String user, String shortDesc, String longDesc,
			String name) {
		super(id, created, user, shortDesc, longDesc, name);
	}

}
