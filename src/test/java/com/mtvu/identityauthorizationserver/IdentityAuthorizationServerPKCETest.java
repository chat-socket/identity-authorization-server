package com.mtvu.identityauthorizationserver;

import com.fasterxml.jackson.databind.JsonNode;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.mtvu.identityauthorizationserver.config.WireMockConfigUserService;
import com.mtvu.identityauthorizationserver.mocks.UserManagementServiceMocks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        classes = IdentityAuthorizationServerApplication.class)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@Import({WireMockConfigUserService.class})
@AutoConfigureMockMvc
public class IdentityAuthorizationServerPKCETest {

    private static final String REDIRECT_URI = "http://127.0.0.1:8080/login/oauth2/code/messaging-client-oidc";

    @Autowired
    private WebClient webClient;

    @Autowired
    private WireMockServer mockUserService;

    private final StringKeyGenerator secureKeyGenerator =
            new Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 96);

    private RestTemplate restTemplate = new RestTemplate();

    @BeforeEach
    public void setUp() throws IOException {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getCookieManager().clearCookies();	// log out
        UserManagementServiceMocks.setupMockBooksResponse(mockUserService);
    }

    @Test
    public void whenRequestAccessTokenWithLoggedUserThenReturnAccessToken() throws NoSuchAlgorithmException, IOException {
        String codeVerifier = secureKeyGenerator.generateKey();
        String codeChallenge = createHash(codeVerifier);
        String codeChallengeMethod = "S256";
        String clientId = "chat-web-client-id";

        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setRedirectEnabled(false);
        signIn(webClient.getPage("/login"), "user1", "password");

        var authorizationRequest = UriComponentsBuilder
                .fromPath("/oauth2/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("scope", "openid")
                .queryParam("state", "some-state")
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", codeChallengeMethod)
                .queryParam("redirect_uri", REDIRECT_URI)
                .toUriString();

        // Request token
        WebResponse response = webClient.getPage(authorizationRequest).getWebResponse();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.MOVED_PERMANENTLY.value());
        String location = response.getResponseHeaderValue("location");
        assertThat(location).startsWith(REDIRECT_URI);
        assertThat(location).contains("code=");

        var redirectUri = UriComponentsBuilder.fromUriString(location).build();
        var authorisationCode = redirectUri.getQueryParams().get("code").get(0);

        var tokenApi = UriComponentsBuilder.fromUriString("http://127.0.0.1:9000/oauth2/token")
                .build().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "authorization_code");
        map.add("code", authorisationCode);
        map.add("code_verifier", codeVerifier);
        map.add("client_id", clientId);
        map.add("redirect_uri", REDIRECT_URI);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<JsonNode> authResponse = restTemplate.postForEntity(tokenApi, request, JsonNode.class);
        Assertions.assertEquals(HttpStatus.OK, authResponse.getStatusCode());

        var accessToken = authResponse.getBody().get("access_token").textValue();
        Assertions.assertNotNull(accessToken);
    }

    private static String createHash(String value) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(value.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

    private static <P extends Page> P signIn(HtmlPage page, String username, String password) throws IOException {
        HtmlInput usernameInput = page.querySelector("input[name=\"username\"]");
        HtmlInput passwordInput = page.querySelector("input[name=\"password\"]");
        HtmlButton signInButton = page.querySelector("button");

        usernameInput.type(username);
        passwordInput.type(password);
        return signInButton.click();
    }
}
