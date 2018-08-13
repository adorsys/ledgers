package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import de.adorsys.ledgers.postings.utils.Ids;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString(callSuper=true)
@Table(uniqueConstraints={@UniqueConstraint(columnNames = {"name", "validFrom"}, name="LedgerAccount_name_validFrom_unique")})
@NoArgsConstructor
public class LedgerAccount extends LedgerEntity {
	
	/*Name of the containing ledger*/
	@Column(nullable=false)
	private String ledger;

	/*Name of the parent of this account in the containing ledger. */
	/*For the root object, the parent carries the name of the object.*/
	@Column(nullable=false)
	private String parent;
	
	/*Reference to the account type from the ledger's chart of account.*/
	@Column(nullable=false)
	private String type;
	
	/*The detail level of this ledger account*/
	private int level;

	@Builder
	public LedgerAccount(String id, String name, LocalDateTime validFrom, LocalDateTime created, String user,
			String ledger, String parent, String type, int level) {
		super(id, name, validFrom, created, user);
		this.ledger = ledger;
		this.parent = parent;
		this.type = type;
		this.level = level;
	}
	
	public static LedgerAccount newChildInstance(String name, LocalDateTime validFrom, String user, LedgerAccount parent, LedgerAccountType type){
		return new LedgerAccount(Ids.id(), 
				name, 
				validFrom, 
				LocalDateTime.now(), 
				user,
				parent.getLedger(),
				parent.getName(),
				type.getName(),
				parent.getLevel()+1);
	}

	public static LedgerAccount clone(LedgerAccount in) {
		return new LedgerAccount(in.getId(), 
				in.getName(), 
				in.getValidFrom(), 
				in.getCreated(), 
				in.getUser(),
				in.getLedger(),
				in.getParent(),
				in.getType(),
				in.getLevel());
	}

	public static List<LedgerAccount> clone(List<LedgerAccount> in) {
		return in.stream().map(coa -> clone(coa)).collect(Collectors.toList());
	}
}
