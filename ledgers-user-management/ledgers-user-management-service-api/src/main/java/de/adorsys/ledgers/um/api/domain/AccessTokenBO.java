package de.adorsys.ledgers.um.api.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccessTokenBO {
	
	private String sub;
	
	private String jti;
	
	private String login;
	
	private AisConsentBO consent;
	
	@JsonProperty("account_accesses")
    private List<AccountAccessBO> accountAccesses = new ArrayList<>();
    
    private UserRoleBO role;
    
    private Date iat;
    
    private Date exp;

	private Map<String, String> act = new HashMap<>();
	
	@JsonProperty("sca_id")
	private String scaId;
	
	@JsonProperty("authorisation_id")
	private String authorisationId;

	@JsonProperty("token_usage")
	private TokenUsageBO tokenUsage;

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

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
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

	public Map<String, String> getAct() {
		return act;
	}

	public void setAct(Map<String, String> act) {
		this.act = act;
	}

	public String getScaId() {
		return scaId;
	}

	public void setScaId(String scaId) {
		this.scaId = scaId;
	}

	public String getAuthorisationId() {
		return authorisationId;
	}

	public void setAuthorisationId(String authorisationId) {
		this.authorisationId = authorisationId;
	}

	public TokenUsageBO getTokenUsage() {
		return tokenUsage;
	}

	public void setTokenUsage(TokenUsageBO tokenUsage) {
		this.tokenUsage = tokenUsage;
	}
	
}
