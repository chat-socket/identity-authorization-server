package com.mtvu.identityauthorizationserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

/**
 * @author mvu
 * @project chat-socket
 **/
@Data
@ConfigurationProperties(prefix = "chat.client")
public class ClientConfigurationProperties {
    private String clientId;
    private String clientSecret;
    private Set<String> scope;
    private String redirectUri;
}
