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

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.mtvu.identityauthorizationserver.config.WireMockConfigUserService;
import com.mtvu.identityauthorizationserver.mocks.UserManagementServiceMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

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
public class IdentityAuthorizationServerRegisterTests {
	@Autowired
	private WebClient webClient;

	@Autowired
	private WireMockServer mockUserService;

	@BeforeEach
	public void setUp() throws IOException {
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
		webClient.getOptions().setRedirectEnabled(true);
		webClient.getCookieManager().clearCookies();	// log out
		UserManagementServiceMocks.setupMockUserCreationResponse(mockUserService, "user@chat-socket.io");
	}

	@Test
	public void whenRegisterSuccessfulThenRedirectToLoginPage() throws IOException {
		HtmlPage page = webClient.getPage("/register");
		assertRegisterPage(page);

		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setRedirectEnabled(false);
		WebResponse signUpResponse = signUp(page, "user@chat-socket.io", "password", "abc").getWebResponse();
		assertThat(signUpResponse.getStatusCode()).isEqualTo(HttpStatus.MOVED_PERMANENTLY.value());	// there is no "default" index page

		String location = signUpResponse.getResponseHeaderValue("location");
		assertThat(location).isEqualTo("/login");
	}

	private static <P extends Page> P signUp(HtmlPage page, String username, String password, String fullName)
			throws IOException {
		HtmlInput usernameInput = page.querySelector("input[name=\"userId\"]");
		HtmlInput passwordInput = page.querySelector("input[name=\"password\"]");
		HtmlInput fullNameInput = page.querySelector("input[name=\"fullName\"]");
		HtmlButton signUp = page.querySelector("button[name=\"signup\"]");

		usernameInput.type(username);
		passwordInput.type(password);
		fullNameInput.type(fullName);
		return signUp.click();
	}

	private static void assertRegisterPage(HtmlPage page) {
		assertThat(page.getUrl().toString()).endsWith("/register");

		HtmlInput usernameInput = page.querySelector("input[name=\"userId\"]");
		HtmlInput passwordInput = page.querySelector("input[name=\"password\"]");
		HtmlInput fullName = page.querySelector("input[name=\"fullName\"]");
		HtmlButton signUpButton = page.querySelector("button[name=\"signup\"]");

		assertThat(usernameInput).isNotNull();
		assertThat(passwordInput).isNotNull();
		assertThat(fullName).isNotNull();
		assertThat(signUpButton.getTextContent().strip()).isEqualTo("Sign up");
	}
}
