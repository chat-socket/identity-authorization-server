package com.mtvu.identityauthorizationserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.mtvu.identityauthorizationserver.config.DefaultDataInitializingConfig;
import com.mtvu.identityauthorizationserver.config.WireMockConfigUserService;
import com.mtvu.identityauthorizationserver.mocks.UserManagementServiceMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Consent page integration tests for the sample Authorization Server serving a custom Consent page.
 *
 * @author Dmitriy Dubson
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@Import({WireMockConfigUserService.class, DefaultDataInitializingConfig.class})
@AutoConfigureMockMvc
public class IdentityAuthorizationServerConsentTests {
    @Autowired
    private WebClient webClient;

    @MockBean
    private OAuth2AuthorizationConsentService authorizationConsentService;

    @Autowired
    private WireMockServer mockUserService;

    private final String redirectUri = "http://127.0.0.1:4200/index.html";

    private final String authorizationRequestUri = UriComponentsBuilder
        .fromPath("/oauth2/authorize")
        .queryParam("response_type", "code")
        .queryParam("client_id", "messaging-client")
        .queryParam("scope", "openid message.read message.write")
        .queryParam("state", "state")
        .queryParam("redirect_uri", this.redirectUri)
        .toUriString();

    @BeforeEach
    public void setUp() throws IOException {
        this.webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        this.webClient.getOptions().setRedirectEnabled(true);
        this.webClient.getCookieManager().clearCookies();
        when(this.authorizationConsentService.findById(any(), any())).thenReturn(null);
        UserManagementServiceMocks.setupMockUserFindResponse(mockUserService, "user@chat-socket.io");

    }

    @Test
    @WithMockUser("user@chat-socket.io")
    public void whenUserConsentsToAllScopesThenReturnAuthorizationCode() throws IOException {
        final HtmlPage consentPage = this.webClient.getPage(this.authorizationRequestUri);
        assertThat(consentPage.getTitleText()).isEqualTo("Custom consent page - Consent required");

        List<HtmlCheckBoxInput> scopes = new ArrayList<>();
        consentPage.querySelectorAll("input[name='scope']").forEach(scope ->
            scopes.add((HtmlCheckBoxInput) scope));
        for (HtmlCheckBoxInput scope : scopes) {
            scope.click();
        }

        List<String> scopeIds = new ArrayList<>();
        scopes.forEach(scope -> {
            assertThat(scope.isChecked()).isTrue();
            scopeIds.add(scope.getId());
        });
        assertThat(scopeIds).containsExactlyInAnyOrder("message.read", "message.write");

        DomElement submitConsentButton = consentPage.querySelector("button[id='submit-consent']");
        this.webClient.getOptions().setRedirectEnabled(false);

        WebResponse approveConsentResponse = submitConsentButton.click().getWebResponse();
        assertThat(approveConsentResponse.getStatusCode()).isEqualTo(HttpStatus.MOVED_PERMANENTLY.value());
        String location = approveConsentResponse.getResponseHeaderValue("location");
        assertThat(location).startsWith(this.redirectUri);
        assertThat(location).contains("code=");
    }

    @Test
    @WithMockUser("user@chat-socket.io")
    public void whenUserCancelsConsentThenReturnAccessDeniedError() throws IOException {
        final HtmlPage consentPage = this.webClient.getPage(this.authorizationRequestUri);
        assertThat(consentPage.getTitleText()).isEqualTo("Custom consent page - Consent required");

        DomElement cancelConsentButton = consentPage.querySelector("button[id='cancel-consent']");
        this.webClient.getOptions().setRedirectEnabled(false);

        WebResponse cancelConsentResponse = cancelConsentButton.click().getWebResponse();
        assertThat(cancelConsentResponse.getStatusCode()).isEqualTo(HttpStatus.MOVED_PERMANENTLY.value());
        String location = cancelConsentResponse.getResponseHeaderValue("location");
        assertThat(location).startsWith(this.redirectUri);
        assertThat(location).contains("error=access_denied");
    }

}
