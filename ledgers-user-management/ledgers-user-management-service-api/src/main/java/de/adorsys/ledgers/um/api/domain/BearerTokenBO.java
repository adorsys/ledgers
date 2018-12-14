package de.adorsys.ledgers.um.api.domain;

public class BearerTokenBO {

	private String access_token;
	
	private String token_type = "Bearer";
	
	private int expires_in;
	
	private String refresh_token;
	
	private AccessTokenBO accessTokenObject;
	
	public BearerTokenBO(String access_token, int expires_in, String refresh_token,
			AccessTokenBO accessTokenObject) {
		super();
		this.access_token = access_token;
		this.expires_in = expires_in;
		this.refresh_token = refresh_token;
		this.accessTokenObject = accessTokenObject;
	}

	public BearerTokenBO() {
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public String getToken_type() {
		return token_type;
	}

	public void setToken_type(String token_type) {
		this.token_type = token_type;
	}

	public int getExpires_in() {
		return expires_in;
	}

	public void setExpires_in(int expires_in) {
		this.expires_in = expires_in;
	}

	public String getRefresh_token() {
		return refresh_token;
	}

	public void setRefresh_token(String refresh_token) {
		this.refresh_token = refresh_token;
	}

	public AccessTokenBO getAccessTokenObject() {
		return accessTokenObject;
	}

	public void setAccessTokenObject(AccessTokenBO accessTokenObject) {
		this.accessTokenObject = accessTokenObject;
	}

}
