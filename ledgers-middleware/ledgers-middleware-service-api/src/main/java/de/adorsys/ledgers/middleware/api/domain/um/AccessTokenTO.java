package de.adorsys.ledgers.middleware.api.domain.um;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description="The access token object.")
public class AccessTokenTO {
	/*
	 * The subject, see rfc7519. This is generally the id of
	 * the resource owner.
	 */
	@ApiModelProperty(value="The database id of the initiator of this token")
	private String sub;
	
	/*
	 * The unique token identifier, see rfc7519
	 */
	@ApiModelProperty(value="The token identifier")
	private String jti;
	
	/*
	 * The login name of the resource owner (user) on behalf of the holder of this token act.
	 * Correspond to the pusId in psd2.
	 */
	@ApiModelProperty(value="The login name of the initiator of this token")
	private String login;
	
	/*
	 * Consent given by the bearer to the holder of this token.
	 */
	@ApiModelProperty(value="The specification of psd2 account access permission associated with this token")
	private AisConsentTO consent;
	
	/*
	 * List of accounts that can be accessed by this token.
	 */
	@ApiModelProperty(value="List of accounts that can be accessed using this token.")
	@JsonProperty("account_accesses")
    private List<AccountAccessTO> accountAccesses = new ArrayList<>();
    
    /*
     * The associated with the token.
     */
	@ApiModelProperty(value="Role to be inforced when this token is presented.")
    private UserRoleTO role;
    
    /*
     * issued at. see rfc7519
     */
	@ApiModelProperty(value="Issue time")
    private Date iat;
    
    /*
     * Expiration. see rfc7519
     */
	@ApiModelProperty(value="expiration time")
    private Date exp;
	
	@ApiModelProperty(value="The bearer this token.")
	private Map<String, String> act = new HashMap<>();

	@ApiModelProperty(value="The id of the sca object: login, payment, account access")
	@JsonProperty("sca_id")
	private String scaId;
	
	@ApiModelProperty(value="The last authorisation id leading to this token")
	@JsonProperty("authorisation_id")
	private String authorisationId;
	
	@ApiModelProperty(value="The usage of this token.")
	@JsonProperty("token_usage")
	private TokenUsageTO tokenUsage;
	
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

	public void setLogin(String actor) {
		this.login = actor;
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

	public TokenUsageTO getTokenUsage() {
		return tokenUsage;
	}

	public void setTokenUsage(TokenUsageTO tokenUsage) {
		this.tokenUsage = tokenUsage;
	}
	
}
