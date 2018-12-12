package de.adorsys.ledgers.middleware.api.domain.um;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AccessTokenTO {
	
	private String sub;
	
	private String jti;
	
	private String actor;
	
	private AisConsentTO consent;
	
    private List<AccountAccessTO> accountAccesses = new ArrayList<>();
    
    private UserRoleTO role;
    
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

	public AisConsentTO getConsent() {
		return consent;
	}

	public void setConsent(AisConsentTO consent) {
		this.consent = consent;
	}

	public List<AccountAccessTO> getAccountAccesses() {
		return accountAccesses;
	}

	public void setAccountAccesses(List<AccountAccessTO> accountAccesses) {
		this.accountAccesses = accountAccesses;
	}

	public UserRoleTO getRole() {
		return role;
	}

	public void setRole(UserRoleTO role) {
		this.role = role;
	}
	
}
