package org.sagebionetworks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sagebionetworks.client.SynapseAdminClient;
import org.sagebionetworks.client.SynapseAdminClientImpl;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.SynapseClientImpl;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.auth.JSONWebTokenHelper;
import org.sagebionetworks.repo.model.oauth.JsonWebKeySet;
import org.sagebionetworks.repo.model.oauth.OAuthAuthorizationResponse;
import org.sagebionetworks.repo.model.oauth.OAuthClient;
import org.sagebionetworks.repo.model.oauth.OAuthClientIdAndSecret;
import org.sagebionetworks.repo.model.oauth.OAuthClientList;
import org.sagebionetworks.repo.model.oauth.OAuthGrantType;
import org.sagebionetworks.repo.model.oauth.OAuthResponseType;
import org.sagebionetworks.repo.model.oauth.OAuthScope;
import org.sagebionetworks.repo.model.oauth.OIDCAuthorizationRequest;
import org.sagebionetworks.repo.model.oauth.OIDCAuthorizationRequestDescription;
import org.sagebionetworks.repo.model.oauth.OIDCSigningAlgorithm;
import org.sagebionetworks.repo.model.oauth.OIDCTokenResponse;
import org.sagebionetworks.repo.model.oauth.OIDConnectConfiguration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwt;

public class ITOpenIDConnectTest {

	private static SynapseAdminClient adminSynapse;
	private static SynapseClient synapseOne;
	private static SynapseAdminClient synapseAnonymous;
	private static Long user1ToDelete;
	private static Long user2ToDelete;
	
	private String clientToDelete;
	
	@BeforeAll
	public static void beforeClass() throws Exception {
		// Create 2 users
		adminSynapse = new SynapseAdminClientImpl();
		SynapseClientHelper.setEndpoints(adminSynapse);
		adminSynapse.setUsername(StackConfigurationSingleton.singleton().getMigrationAdminUsername());
		adminSynapse.setApiKey(StackConfigurationSingleton.singleton().getMigrationAdminAPIKey());
		adminSynapse.clearAllLocks();
		synapseOne = new SynapseClientImpl();
		SynapseClientHelper.setEndpoints(synapseOne);
		user1ToDelete = SynapseClientHelper.createUser(adminSynapse, synapseOne);
		
		synapseAnonymous = new SynapseAdminClientImpl();
		SynapseClientHelper.setEndpoints(synapseAnonymous);
	}
	
	@AfterAll
	public static void afterClass() throws Exception {
		try {
			if (user1ToDelete!=null) adminSynapse.deleteUser(user1ToDelete);
		} catch (SynapseException e) { }
		try {
			if (user2ToDelete!=null) adminSynapse.deleteUser(user2ToDelete);
		} catch (SynapseException e) { }
	}

	@AfterEach
	public void after() throws Exception {
		try {
			if (clientToDelete!=null) {
				synapseOne.deleteOAuthClient(clientToDelete);
			}
		} catch (SynapseException e) {
			// already gone
		}
	}

	
	@Test
	public void testRoundTrip() throws Exception {
		OIDConnectConfiguration connectConfig = synapseAnonymous.getOIDConnectConfiguration();
		assertNotNull(connectConfig.getIssuer());
		
		JsonWebKeySet jsonWebKeySet = synapseAnonymous.getOIDCJsonWebKeySet();
		assertFalse(jsonWebKeySet.getKeys().isEmpty());
		
		OAuthClient client = new OAuthClient();
		client.setClient_name("some client");
		client.setRedirect_uris(Collections.singletonList("https://foo.bar.com"));
		client = synapseOne.createOAuthClient(client);
		clientToDelete = client.getClient_id();
		
		assertEquals(client, synapseOne.getOAuthClient(client.getClient_id()));
		
		OAuthClientList clientList = synapseOne.listOAuthClients(null);
		assertEquals(client, clientList.getResults().get(0));
		
		OAuthClientIdAndSecret secret = synapseOne.createOAuthClientSecret(client.getClient_id());
		assertEquals(client.getClient_id(), secret.getClient_id());
		assertNotNull(secret.getClient_secret());
				
		OIDCAuthorizationRequest authorizationRequest = new OIDCAuthorizationRequest();
		authorizationRequest.setClientId(client.getClient_id());
		authorizationRequest.setRedirectUri(client.getRedirect_uris().get(0));
		authorizationRequest.setResponseType(OAuthResponseType.code);
		authorizationRequest.setScope("openid");
		authorizationRequest.setClaims(
				"{\"id_token\":{\"userid\":\"null\",\"email\":null,\"is_certified\":null,\"team\":{\"values\":[\"2\"]}},"+
				 "\"userinfo\":{\"userid\":\"null\",\"email\":null,\"is_certified\":null,\"team\":{\"values\":[\"2\"]}}}"
		);
		
		// Note, we get the authorization description anonymously
		OIDCAuthorizationRequestDescription description = 
				synapseAnonymous.getAuthenticationRequestDescription(authorizationRequest);
		// make sure we got something back
		assertFalse(description.getScope().isEmpty());
		
		OAuthAuthorizationResponse oauthAuthorizationResponse = synapseOne.authorizeClient(authorizationRequest);
		
		// Note, we use Basic auth to authorize the client when asking for an access token
		OIDCTokenResponse tokenResponse = null;
		try {
			synapseAnonymous.setBasicAuthorizationCredentials(client.getClient_id(), secret.getClient_secret());
			tokenResponse = synapseAnonymous.getTokenResponse(OAuthGrantType.authorization_code, 
					oauthAuthorizationResponse.getAccess_code(), client.getRedirect_uris().get(0), null, null, null);
		} finally {
			synapseAnonymous.removeAuthorizationHeader();
		}
		
		Jwt<JwsHeader, Claims> parsedIdToken = JSONWebTokenHelper.parseJWT(tokenResponse.getId_token(), jsonWebKeySet);
		UserProfile myProfile = synapseOne.getMyProfile();
		String myId = myProfile.getOwnerId();
		Claims idClaims = parsedIdToken.getBody();
		assertEquals(myId, idClaims.get("userid", String.class));
		assertTrue(idClaims.get("is_certified", Boolean.class));
		String email = myProfile.getEmails().get(0);
		assertEquals(email, idClaims.get("email", String.class));
		assertEquals(Collections.EMPTY_LIST, idClaims.get("team", List.class));
		
		// the access token encodes claims we can refresh
		Jwt<JwsHeader, Claims> parsedAccessToken = JSONWebTokenHelper.parseJWT(tokenResponse.getAccess_token(), jsonWebKeySet);
		Claims accessClaims = parsedAccessToken.getBody();
		Map access = (Map)accessClaims.get("access", Map.class);
		List<String> userInfoScope = (List<String>)access.get("scope");
		assertEquals(1, userInfoScope.size());
		assertEquals(OAuthScope.openid.name(), userInfoScope.get(0));
		Map userInfoClaims = (Map)access.get("oidc_claims");
		assertTrue(userInfoClaims.containsKey("userid"));
		assertTrue(userInfoClaims.containsKey("email"));
		assertTrue(userInfoClaims.containsKey("is_certified"));
		assertTrue(userInfoClaims.containsKey("team"));

		// Note, we use a bearer token to authorize the client 
		try {
			synapseAnonymous.setBearerAuthorizationToken(tokenResponse.getAccess_token());
			JSONObject userInfo = synapseAnonymous.getUserInfoAsJSON();
			// check userInfo
			assertEquals(myId, (String)userInfo.get("userid"));
			assertEquals(email, (String)userInfo.get("email"));
			assertTrue((Boolean)userInfo.get("is_certified"));
			assertEquals(0, ((JSONArray)userInfo.get("team")).length());
		} finally {
			synapseAnonymous.removeAuthorizationHeader();
		}
		
		OAuthClient retrieved = synapseOne.getOAuthClient(client.getClient_id());
		retrieved.setUserinfo_signed_response_alg(OIDCSigningAlgorithm.RS256);
		OAuthClient updated = synapseOne.updateOAuthClient(retrieved);
		assertEquals(retrieved.getClient_name(), updated.getClient_name());
		
		// Note, we use a bearer token to authorize the client 
		try {
			synapseAnonymous.setBearerAuthorizationToken(tokenResponse.getAccess_token());
			Jwt<JwsHeader,Claims> userInfo = synapseAnonymous.getUserInfoAsJSONWebToken();
			Claims body = userInfo.getBody();
			assertEquals(myId, body.get("userid", String.class));
			assertEquals(email, body.get("email", String.class));
			assertTrue(body.get("is_certified", Boolean.class));
			assertEquals(Collections.EMPTY_LIST, body.get("team", List.class));
		} finally {
			synapseAnonymous.removeAuthorizationHeader();
		}
		
		synapseOne.deleteOAuthClient(client.getClient_id());
		clientToDelete=null;
	}

}
