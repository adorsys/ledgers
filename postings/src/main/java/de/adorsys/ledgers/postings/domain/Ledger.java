package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import de.adorsys.ledgers.postings.basetypes.LedgerName;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString(callSuper=true)
@NoArgsConstructor
@Table(uniqueConstraints={@UniqueConstraint(columnNames = {"name", "validFrom"}, name="Ledger_name_validFrom_unique")})
public class Ledger extends LedgerEntity {
	/*Name of the attached chart of account.*/
	@Column(nullable=false)
	private String coa;

	@Builder
	public Ledger(String id, String name, LocalDateTime validFrom, LocalDateTime created, String user, String coa) {
		super(id, name, validFrom, created, user);
		this.coa = coa;
	}
	
	public static Ledger clone(Ledger in) {
		return in==null?null:new Ledger(in.getId(), 
				in.getName(), 
				in.getValidFrom(), 
				in.getCreated(), 
				in.getUser(),
				in.getCoa());
	}

	public static List<Ledger> clone(List<Ledger> in) {
		return in.stream().map(l -> clone(l)).collect(Collectors.toList());
	}
	
	public LedgerName toName(){
		return new LedgerName(getName());
	}
}
