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

import com.mtvu.identityauthorizationserver.auth.ServiceAuthenticationProvider;
import com.mtvu.identityauthorizationserver.auth.UserDetailsWithPasswordService;
import com.mtvu.identityauthorizationserver.security.FederatedIdentityConfigurer;
import com.mtvu.identityauthorizationserver.security.UserRepositoryOAuth2UserHandler;
import feign.Contract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * @author Steve Riesenberg
 * @author mvu
 * @project chat-socket
 * @since 0.2.3
 */
@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class DefaultSecurityConfig {

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

	// @formatter:off
	@Bean
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http,
														  UserDetailsWithPasswordService userService,
														  CorsConfigurationSource corsConfigurationSource) throws Exception {
		FederatedIdentityConfigurer federatedIdentityConfigurer = new FederatedIdentityConfigurer()
			.oauth2UserHandler(new UserRepositoryOAuth2UserHandler());

		var serviceAuthenticationProvider = new ServiceAuthenticationProvider();
		serviceAuthenticationProvider.setUserDetailsWithPasswordService(userService);
		http
			.cors()
				.configurationSource(corsConfigurationSource)
			.and()
			.authorizeHttpRequests(authorize ->
				authorize
					.requestMatchers("/assets/**", "/webjars/**", "/actuator/**",
							"/register", "/login").permitAll()
					.anyRequest().authenticated()
			)
			.formLogin(Customizer.withDefaults())
			.authenticationProvider(serviceAuthenticationProvider)
			.apply(federatedIdentityConfigurer);
		return http.build();
	}
	// @formatter:on

	@Bean
	public Contract feignContract() {
		return new feign.Contract.Default();
	}
}
