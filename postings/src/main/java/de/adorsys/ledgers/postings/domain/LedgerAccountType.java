package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import de.adorsys.ledgers.postings.basetypes.LedgerAccountTypeName;
import de.adorsys.ledgers.postings.utils.Ids;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString(callSuper=true)
@NoArgsConstructor
@Table(uniqueConstraints={@UniqueConstraint(columnNames = {"name", "validFrom"}, name="LedgerAccountType_name_validFrom_unique")})
public class LedgerAccountType extends LedgerEntity {
	
	/*Containing chart of account.*/
	@Column(nullable=false)
	private String coa;

	/*For the root object, the parent carries the name of the object.*/
	@Column(nullable=false)
	private String parent;

	/*The detail level of this ledger account type*/
	private int level;
	
	@Builder
	public LedgerAccountType(String id, String name, LocalDateTime validFrom, LocalDateTime created, String user,
			String coa, String parent, int level) {
		super(id, name, validFrom, created, user);
		this.coa = coa;
		this.parent = parent;
		this.level = level;
	}

	public static LedgerAccountType newChildInstance(String name, LocalDateTime validFrom, String user, LedgerAccountType parent){
		return new LedgerAccountType(Ids.id(), 
				name, 
				validFrom, 
				LocalDateTime.now(), 
				user,
				parent.getCoa(),
				parent.getName(),
				parent.getLevel()+1);
	}

	public static LedgerAccountType clone(LedgerAccountType in) {
		return in==null?null:new LedgerAccountType(in.getId(), 
				in.getName(), 
				in.getValidFrom(), 
				in.getCreated(), 
				in.getUser(),
				in.getCoa(),
				in.getParent(),
				in.getLevel());
	}

	public static List<LedgerAccountType> clone(List<LedgerAccountType> in) {
		return in.stream().map(lat -> clone(lat)).collect(Collectors.toList());
	}
	
	public LedgerAccountTypeName toName(){
		return new LedgerAccountTypeName(getName());
	}
	
}
