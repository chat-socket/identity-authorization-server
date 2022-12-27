package com.mtvu.identityauthorizationserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Set;

/**
 * @author mvu
 * @project chat-socket
 **/
@Data
@ConfigurationProperties(prefix = "chat")
public class ClientConfigurationProperties {

    private List<ClientProperties> clients;

    @Data
    public static class ClientProperties {

        private String identifier;
        private String clientId;
        private String clientSecret;
        private Set<String> scope;
        private String redirectUri;
    }
}
