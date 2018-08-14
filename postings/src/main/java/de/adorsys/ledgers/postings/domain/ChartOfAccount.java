package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import de.adorsys.ledgers.postings.basetypes.ChartOfAccountName;
import de.adorsys.ledgers.postings.utils.Ids;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString(callSuper=true)
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
@Table(uniqueConstraints={@UniqueConstraint(columnNames = {"name", "validFrom"}, name="ChartOfAccount_name_validFrom_unique")})
public class ChartOfAccount extends LedgerEntity {
	@Builder
	public ChartOfAccount(String id, String name, LocalDateTime validFrom, LocalDateTime created, String user) {
		super(id, name, validFrom, created, user);
	}
	
	public static ChartOfAccount newChartOfAccount(ChartOfAccount chartOfAccount, String user){
		return new ChartOfAccount(Ids.id(), 
				chartOfAccount.getName(), 
				chartOfAccount.getValidFrom(), 
				LocalDateTime.now(), user);
	}
	
	public ChartOfAccountName toName(){
		return new ChartOfAccountName(getName());
	}
}
