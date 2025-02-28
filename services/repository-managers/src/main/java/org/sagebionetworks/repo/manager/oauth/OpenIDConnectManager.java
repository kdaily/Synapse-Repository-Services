package org.sagebionetworks.repo.manager.oauth;

import org.sagebionetworks.repo.model.UserInfo;
import org.sagebionetworks.repo.model.oauth.OAuthAuthorizationResponse;
import org.sagebionetworks.repo.model.oauth.OIDCAuthorizationRequest;
import org.sagebionetworks.repo.model.oauth.OIDCAuthorizationRequestDescription;
import org.sagebionetworks.repo.model.oauth.OIDCTokenResponse;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwt;

/**
 *
 */
public interface OpenIDConnectManager {
	/**
	 * 
	 * @param authorizationRequest
	 * @return
	 */
	OIDCAuthorizationRequestDescription getAuthenticationRequestDescription(OIDCAuthorizationRequest authorizationRequest);
	
	/**
	 * 
	 * @param userInfo
	 * @param authorizationRequest
	 * @return
	 */
	OAuthAuthorizationResponse authorizeClient(UserInfo userInfo, OIDCAuthorizationRequest authorizationRequest);
	
	/**
	 * 
	 * @param authorizationCode
	 * @param verifiedClientId Client ID verified via client authentication
	 * @param redirectUri
	 * @return
	 */
	OIDCTokenResponse getAccessToken(String authorizationCode, String verifiedClientId, String redirectUri, String oauthEndpoint);
	
	/**
	 * 
	 * Given the validated access token content, return the up-to-date user info
	 * requested in the scopes / claims embedded in the access token

	 * @param accessToken
	 * @return either a JWT or a JSON Object, depending on whether the client registered a value for
	 * userinfo_signed_response_alg
	 */
	Object getUserInfo(Jwt<JwsHeader,Claims> accessToken, String oauthEndpoint);

}
