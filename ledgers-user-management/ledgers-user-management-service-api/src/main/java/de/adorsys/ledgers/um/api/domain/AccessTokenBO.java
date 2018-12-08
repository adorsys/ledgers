package de.adorsys.ledgers.um.api.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AccessTokenBO {
	
	private String sub;
	
	private String jti;
	
	private String actor;
	
	private AisConsentBO consent;
	
    private List<AccountAccessBO> accountAccesses = new ArrayList<>();
    
    private UserRoleBO role;
    
    private Date iat;
    
    private Date exp;

	public String getSub() {
		return sub;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}

	public String getJti() {
		return jti;
	}

	public void setJti(String jti) {
		this.jti = jti;
	}

	public String getActor() {
		return actor;
	}

	public void setActor(String actor) {
		this.actor = actor;
	}

	public AisConsentBO getConsent() {
		return consent;
	}

	public void setConsent(AisConsentBO consent) {
		this.consent = consent;
	}

	public List<AccountAccessBO> getAccountAccesses() {
		return accountAccesses;
	}

	public void setAccountAccesses(List<AccountAccessBO> accountAccesses) {
		this.accountAccesses = accountAccesses;
	}

	public UserRoleBO getRole() {
		return role;
	}

	public void setRole(UserRoleBO role) {
		this.role = role;
	}

	public Date getIat() {
		return iat;
	}

	public void setIat(Date iat) {
		this.iat = iat;
	}

	public Date getExp() {
		return exp;
	}

	public void setExp(Date exp) {
		this.exp = exp;
	}
}
