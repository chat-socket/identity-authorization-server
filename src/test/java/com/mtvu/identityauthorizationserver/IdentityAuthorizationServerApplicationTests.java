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
import com.mtvu.identityauthorizationserver.config.DefaultDataInitializingConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@Import({DefaultDataInitializingConfig.class})
@AutoConfigureMockMvc
public class IdentityAuthorizationServerApplicationTests {
	private static final String REDIRECT_URI = "http://127.0.0.1:8080/login/oauth2/code/messaging-client-oidc";

	private static final String AUTHORIZATION_REQUEST = UriComponentsBuilder
			.fromPath("/oauth2/authorize")
			.queryParam("response_type", "code")
			.queryParam("client_id", "messaging-client")
			.queryParam("scope", "openid")
			.queryParam("state", "some-state")
			.queryParam("redirect_uri", REDIRECT_URI)
			.toUriString();

	@Autowired
	private WebClient webClient;

	@LocalServerPort
	private int appPort;

	private RestTemplate restTemplate = new RestTemplate();

	@BeforeEach
	public void setUp() {
		this.webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
		this.webClient.getOptions().setRedirectEnabled(true);
		this.webClient.getCookieManager().clearCookies();	// log out

	}

	@Test
	public void whenLoginSuccessfulThenDisplayNotFoundError() throws IOException {
		HtmlPage page = this.webClient.getPage("/");

		assertLoginPage(page);

		this.webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		WebResponse signInResponse = signIn(page, "user1", "password").getWebResponse();
		assertThat(signInResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());	// there is no "default" index page
	}

	@Test
	public void whenLoginFailsThenDisplayBadCredentials() throws IOException {
		HtmlPage page = this.webClient.getPage("/");

		HtmlPage loginErrorPage = signIn(page, "user1", "wrong-password");

		HtmlElement alert = loginErrorPage.querySelector("div[role=\"alert\"]");
		assertThat(alert).isNotNull();
		assertThat(alert.getTextContent().strip()).isEqualTo("Invalid username or password.");
	}

	@Test
	public void whenNotLoggedInAndRequestingTokenThenRedirectsToLogin() throws IOException {
		HtmlPage page = this.webClient.getPage(AUTHORIZATION_REQUEST);

		assertLoginPage(page);
	}

	@Test
	public void whenLoggingInAndRequestingTokenThenRedirectsToClientApplication() throws IOException {
		// Log in
		this.webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		this.webClient.getOptions().setRedirectEnabled(false);
		signIn(this.webClient.getPage("/login"), "user1", "password");

		// Request token
		WebResponse response = this.webClient.getPage(AUTHORIZATION_REQUEST).getWebResponse();

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.MOVED_PERMANENTLY.value());
		String location = response.getResponseHeaderValue("location");
		assertThat(location).startsWith(REDIRECT_URI);
		assertThat(location).contains("code=");
	}

	@Test
	public void whenLoggingInAndRequestingTokenThenReturnAccessToken() throws IOException {

		this.webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		this.webClient.getOptions().setRedirectEnabled(false);
		signIn(this.webClient.getPage("/login"), "user1", "password");

		// Request token
		WebResponse response = this.webClient.getPage(AUTHORIZATION_REQUEST).getWebResponse();

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.MOVED_PERMANENTLY.value());
		String location = response.getResponseHeaderValue("location");
		assertThat(location).startsWith(REDIRECT_URI);
		assertThat(location).contains("code=");

		var redirectUri = UriComponentsBuilder.fromUriString(location).build();
		var authorisationCode = redirectUri.getQueryParams().get("code").get(0);

		var tokenApi = UriComponentsBuilder.newInstance()
				.scheme("http")
				.host("127.0.0.1")
				.port(appPort)
				.path("/oauth2/token").build().toUri();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
		var encodedClientData = Base64Utils.encodeToString("messaging-client:secret".getBytes());
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
		HtmlButton signInButton = page.querySelector("button");

		usernameInput.type(username);
		passwordInput.type(password);
		return signInButton.click();
	}

	private static void assertLoginPage(HtmlPage page) {
		assertThat(page.getUrl().toString()).endsWith("/login");

		HtmlInput usernameInput = page.querySelector("input[name=\"username\"]");
		HtmlInput passwordInput = page.querySelector("input[name=\"password\"]");
		HtmlButton signInButton = page.querySelector("button");

		assertThat(usernameInput).isNotNull();
		assertThat(passwordInput).isNotNull();
		assertThat(signInButton.getTextContent()).isEqualTo("Sign in");
	}

}
