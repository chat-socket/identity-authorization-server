/*
 * Copyright 2020-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mtvu.identityauthorizationserver;

import java.io.IOException;
import java.util.Collections;

import com.fasterxml.jackson.databind.JsonNode;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the sample Authorization Server.
 *
 * @author Daniel Garnier-Moiroux
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
		classes = IdentityAuthorizationServerApplication.class)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@Import({WireMockConfigUserService.class})
@AutoConfigureMockMvc
public class IdentityAuthorizationServerAuthenticationTests {
	private static final String REDIRECT_URI = "http://127.0.0.1:4200/callback.html";

	private static final String AUTHORIZATION_REQUEST = UriComponentsBuilder
			.fromPath("/oauth2/authorize")
			.queryParam("response_type", "code")
			.queryParam("client_id", "chat-web-client-id")
			.queryParam("scope", "openid")
			.queryParam("state", "some-state")
			.queryParam("redirect_uri", REDIRECT_URI)
			.toUriString();

	@Autowired
	private WebClient webClient;

	@Autowired
	private WireMockServer mockUserService;

	private RestTemplate restTemplate = new RestTemplate();

	@BeforeEach
	public void setUp() throws IOException {
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
		webClient.getOptions().setRedirectEnabled(true);
		webClient.getCookieManager().clearCookies();	// log out
		UserManagementServiceMocks.setupMockUserFindResponse(mockUserService, "user@chat-socket.io", "password");
	}

	@Test
	public void whenLoginSuccessfulThenDisplayNotFoundError() throws IOException {
		HtmlPage page = webClient.getPage("/");

		assertLoginPage(page);

		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		WebResponse signInResponse = signIn(page, "user@chat-socket.io", "password").getWebResponse();
		assertThat(signInResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());	// there is no "default" index page
	}

	@Test
	public void whenLoginFailsThenDisplayBadCredentials() throws IOException {
		HtmlPage page = webClient.getPage("/");

		HtmlPage loginErrorPage = signIn(page, "user@chat-socket.io", "wrong-password");

		HtmlElement alert = loginErrorPage.querySelector("div[role=\"alert\"]");
		assertThat(alert).isNotNull();
		assertThat(alert.getTextContent().strip()).isEqualTo("Invalid username or password !");
	}

	@Test
	public void whenNotLoggedInAndRequestingTokenThenRedirectsToLogin() throws IOException {
		HtmlPage page = webClient.getPage(AUTHORIZATION_REQUEST);

		assertLoginPage(page);
	}

	@Test
	public void whenLoggingInAndRequestingTokenThenRedirectsToClientApplication() throws IOException {
		// Log in
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setRedirectEnabled(false);
		signIn(webClient.getPage("/login"), "user@chat-socket.io", "password");

		// Request token
		WebResponse response = webClient.getPage(AUTHORIZATION_REQUEST).getWebResponse();

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.MOVED_PERMANENTLY.value());
		String location = response.getResponseHeaderValue("location");
		assertThat(location).startsWith(REDIRECT_URI);
		assertThat(location).contains("code=");
	}

	@Test
	public void whenLoggingInAndRequestingTokenThenReturnAccessToken() throws IOException {

		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setRedirectEnabled(false);
		signIn(webClient.getPage("/login"), "user@chat-socket.io", "password");

		// Request token
		WebResponse response = webClient.getPage(AUTHORIZATION_REQUEST).getWebResponse();

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
		var encodedClientData = Base64Utils.encodeToString("chat-web-client-id:web-client-secret".getBytes());
		headers.add("Authorization", "Basic " + encodedClientData);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("grant_type", "authorization_code");
		map.add("code", authorisationCode);
		map.add("redirect_uri", REDIRECT_URI);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

		ResponseEntity<JsonNode> authResponse = restTemplate.postForEntity(tokenApi, request, JsonNode.class);
		Assertions.assertEquals(HttpStatus.OK, authResponse.getStatusCode());

		var accessToken = authResponse.getBody().get("access_token").textValue();
		Assertions.assertNotNull(accessToken);
	}

	private static <P extends Page> P signIn(HtmlPage page, String username, String password) throws IOException {
		HtmlInput usernameInput = page.querySelector("input[name=\"username\"]");
		HtmlInput passwordInput = page.querySelector("input[name=\"password\"]");
		HtmlButton signInButton = page.querySelector("button[name=\"signin\"]");

		usernameInput.type(username);
		passwordInput.type(password);
		return signInButton.click();
	}

	private static void assertLoginPage(HtmlPage page) {
		assertThat(page.getUrl().toString()).endsWith("/login");

		HtmlInput usernameInput = page.querySelector("input[name=\"username\"]");
		HtmlInput passwordInput = page.querySelector("input[name=\"password\"]");
		HtmlButton signInButton = page.querySelector("button[name=\"signin\"]");

		assertThat(usernameInput).isNotNull();
		assertThat(passwordInput).isNotNull();
		assertThat(signInButton.getTextContent().strip()).isEqualTo("Sign in");
	}

}
