package de.adorsys.ledgers.middleware.api.domain.um;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BearerTokenTO {

	private String access_token;
	
	private String token_type = "Bearer";
	
	private int expires_in;
	
	private String refresh_token;
	
	private AccessTokenTO accessTokenObject;
}
