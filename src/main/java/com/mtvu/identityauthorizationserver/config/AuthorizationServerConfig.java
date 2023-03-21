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
package com.mtvu.identityauthorizationserver.config;

import com.mtvu.identityauthorizationserver.config.properties.ClientConfigurationProperties;
import com.mtvu.identityauthorizationserver.config.properties.ProviderConfigurationProperties;
import com.mtvu.identityauthorizationserver.jose.Jwks;
import com.mtvu.identityauthorizationserver.security.FederatedIdentityConfigurer;
import com.mtvu.identityauthorizationserver.security.FederatedIdentityIdTokenCustomizer;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Steve Riesenberg
 * @author mvu
 * @project chat-socket
 * @since 0.2.3
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ClientConfigurationProperties.class, ProviderConfigurationProperties.class})
public class AuthorizationServerConfig {
    private static final String CUSTOM_CONSENT_PAGE_URI = "/oauth2/consent";

	@Value("${spring.security.oauth2.server.external-issuer-uri}")
	private String externalIssuer;

	@Value("${spring.security.oauth2.server.internal-issuer-uri}")
	private String internalIssuer;

	@Value("${spring.security.cors.whitelist}")
	private List<String> corsWhiteList;

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public SecurityFilterChain authorizationServerSecurityFilterChain(
			HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
		OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

		http.cors()
				.configurationSource(corsConfigurationSource)
			.and().getConfigurer(OAuth2AuthorizationServerConfigurer.class)
            .authorizationEndpoint(authorizationEndpoint ->
                authorizationEndpoint.consentPage(CUSTOM_CONSENT_PAGE_URI))
            .oidc(Customizer.withDefaults());	// Enable OpenID Connect 1.0

        RequestMatcher endpointsMatcher = http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
            .getEndpointsMatcher();
		http
            .securityMatcher(endpointsMatcher)
			.csrf()
				.ignoringRequestMatchers(endpointsMatcher)
			.and()
				.headers()
				.frameOptions()
				.sameOrigin()
			.and()
            .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
		http.apply(new FederatedIdentityConfigurer());
		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(corsWhiteList);

		config.addAllowedHeader("*");
		config.addAllowedMethod("GET");
		config.addAllowedMethod("POST");

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/oauth2/**", config);
		source.registerCorsConfiguration("/.well-known/**", config);
		source.registerCorsConfiguration("/userinfo", config);

		return source;
	}

	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> idTokenCustomizer() {
		return new FederatedIdentityIdTokenCustomizer();
	}

	@Bean
	public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate,
                                                                 ClientConfigurationProperties clients) {
        var registeredClientRepository = new JdbcRegisteredClientRepository(jdbcTemplate);
		for (ClientConfigurationProperties.ClientProperties properties : clients.getClients()) {
			Set<AuthorizationGrantType> grantTypes = new HashSet<>();
			for (String grantType : properties.getGrantTypes()) {
				grantTypes.add(new AuthorizationGrantType((grantType)));
			}
			RegisteredClient registeredClient = RegisteredClient.withId(properties.getIdentifier())
					.clientId(properties.getClientId())
					.clientSecret(properties.getClientSecret())
					.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
					.clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
					.authorizationGrantTypes(x -> x.addAll(grantTypes))
					.redirectUris(x -> x.addAll(properties.getRedirectUris()))
					.scopes((x) -> x.addAll(properties.getScopes()))
					.tokenSettings(TokenSettings.builder()
							.accessTokenTimeToLive(Duration.ofMinutes(10))
							.refreshTokenTimeToLive(Duration.ofHours(2))
							.build())
					.clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
					.build();
			registeredClientRepository.save(registeredClient);
		}
		return registeredClientRepository;
	}

	@Bean
	public ClientRegistration defaultClientRegistration(ProviderConfigurationProperties provider) {
		return ClientRegistration.withRegistrationId(provider.getIdentifier())
				.clientId(provider.getClientId())
				.tokenUri(provider.getTokenUri())
				.clientSecret(provider.getClientSecret())
				.scope(provider.getScopes())
				.authorizationGrantType(new AuthorizationGrantType(provider.getGrantType()))
				.build();
	}

	@Bean
	public ClientRegistrationRepository clientRegistrationRepository(ClientRegistration clientRegistration) {
		return new InMemoryClientRegistrationRepository(clientRegistration);
	}

	@Bean
	public OAuth2AuthorizedClientService auth2AuthorizedClientService(ClientRegistrationRepository client) {
		return new InMemoryOAuth2AuthorizedClientService(client);
	}

	@Bean
	public AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientServiceAndManager (
			ClientRegistrationRepository clientRegistrationRepository,
			OAuth2AuthorizedClientService authorizedClientService) {

		OAuth2AuthorizedClientProvider authorizedClientProvider =
				OAuth2AuthorizedClientProviderBuilder.builder()
						.clientCredentials()
						.build();

		AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
				new AuthorizedClientServiceOAuth2AuthorizedClientManager(
						clientRegistrationRepository, authorizedClientService);
		authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

		return authorizedClientManager;
	}

	@Bean
	public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
		return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
	}

	@Bean
	public OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
		return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
	}

	@Bean
	public JWKSource<SecurityContext> jwkSource() {
		RSAKey rsaKey = Jwks.generateRsa();
		JWKSet jwkSet = new JWKSet(rsaKey);
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}

	@Bean
	public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		return new JwtMultiIssuerDecoder(jwkSource, externalIssuer, internalIssuer);
	}

	@Bean
	public AuthorizationServerSettings authorizationServerSettings() {
		return AuthorizationServerSettings.builder()
				.build();
	}

}
