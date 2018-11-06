package de.adorsys.ledgers.postings.db.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters.LocalDateTimeConverter;

/**
 * A posting trace document the inclusion of a posting in the creation of a 
 * statement.
 * <p>
 * 
 * For each stmt posting, an operation can only be involved once.
 *
 * @author fpo
 */
@Entity
@Table(uniqueConstraints = {
		@UniqueConstraint(columnNames = { "tgt_pst_id", "src_opr_id" }, name = "PostingTrace_tgt_pst_id_src_opr_id_unique") })
public class PostingTrace {
    @Id
    private String id;

    /*The target posting id. Posting receiving.*/
    @Column(nullable = false, updatable = false, name="tgt_pst_id")
    private String tgtPstId;
    
    @Convert(converter=LocalDateTimeConverter.class)
    private LocalDateTime srcPstTime;

    /*The target posting id. Posting receiving.*/
    @Column(nullable = false)
    private String srcPstId;

    /*The source operation id*/
    @Column(nullable = false, updatable = false, name="src_opr_id")
    private String srcOprId;

	/*The associated ledger account*/
	@ManyToOne(optional=false)
	private LedgerAccount account;
	
	@Column(nullable=false)
	private BigDecimal debitAmount;

	@Column(nullable=false)
	private BigDecimal creditAmount;
	
	private String srcPstHash;
    
	public String getId() {
		return id;
	}

	public String getTgtPstId() {
		return tgtPstId;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setTgtPstId(String tgtPstId) {
		this.tgtPstId = tgtPstId;
	}

	public LedgerAccount getAccount() {
		return account;
	}

	public void setAccount(LedgerAccount account) {
		this.account = account;
	}

	public BigDecimal getDebitAmount() {
		return debitAmount;
	}

	public void setDebitAmount(BigDecimal debitAmount) {
		this.debitAmount = debitAmount;
	}

	public BigDecimal getCreditAmount() {
		return creditAmount;
	}

	public void setCreditAmount(BigDecimal creditAmount) {
		this.creditAmount = creditAmount;
	}

	public LocalDateTime getSrcPstTime() {
		return srcPstTime;
	}

	public void setSrcPstTime(LocalDateTime srcPstTime) {
		this.srcPstTime = srcPstTime;
	}

	public String getSrcOprId() {
		return srcOprId;
	}

	public void setSrcOprId(String srcOprId) {
		this.srcOprId = srcOprId;
	}

	public String getSrcPstHash() {
		return srcPstHash;
	}

	public void setSrcPstHash(String srcPstHash) {
		this.srcPstHash = srcPstHash;
	}

	public String getSrcPstId() {
		return srcPstId;
	}

	public void setSrcPstId(String srcPstId) {
		this.srcPstId = srcPstId;
	}
	
}
