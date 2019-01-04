package de.adorsys.ledgers.middleware.api.domain.um;

public class BearerTokenTO {

	private String access_token;
	
	private String token_type = "Bearer";
	
	private int expires_in;
	
	private String refresh_token;
	
	private AccessTokenTO accessTokenObject;
	
	public BearerTokenTO() {
	}

	public BearerTokenTO(String access_token, int expires_in, String refresh_token,
			AccessTokenTO accessTokenObject) {
		this.access_token = access_token;
		this.expires_in = expires_in;
		this.refresh_token = refresh_token;
		this.accessTokenObject = accessTokenObject;
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

	public AccessTokenTO getAccessTokenObject() {
		return accessTokenObject;
	}

	public void setAccessTokenObject(AccessTokenTO accessTokenObject) {
		this.accessTokenObject = accessTokenObject;
	}

}
