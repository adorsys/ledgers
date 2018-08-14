package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import de.adorsys.ledgers.postings.basetypes.ChartOfAccountName;
import de.adorsys.ledgers.postings.utils.Ids;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString(callSuper=true)
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

	public static ChartOfAccount clone(ChartOfAccount in) {
		return in==null?null:new ChartOfAccount(in.getId(), 
				in.getName(), 
				in.getValidFrom(), 
				in.getCreated(), in.getUser());
	}

	public static List<ChartOfAccount> clone(List<ChartOfAccount> in) {
		return in.stream().map(coa -> clone(coa)).collect(Collectors.toList());
	}
	
	public ChartOfAccountName toName(){
		return new ChartOfAccountName(getName());
	}
}
