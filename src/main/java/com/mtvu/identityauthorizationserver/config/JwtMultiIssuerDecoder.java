package com.mtvu.identityauthorizationserver.config;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;

/**
 * In this class, we try to solve the problem of OAuth2 issuer verification
 * If we set the issuer to the external uri, then the network traffic to go from resource server to the authorization
 * server will be too high to afford. But if we set the issuer uri to the internal one, then we can't access it
 * from outside the cluster.
 * <p>
 * With this class, we allow either external issuer uri or internal issuer
 * See: <a href="https://github.com/spring-projects/spring-security/issues/11515">...</a>
 *
 * @author mvu
 * @project chat-socket
 **/
public class JwtMultiIssuerDecoder implements JwtDecoder {
    private final NimbusJwtDecoder internalDecoder;
    private final NimbusJwtDecoder publicDecoder;

    public JwtMultiIssuerDecoder(JWKSource<SecurityContext> jwkSource, String internalUri, String publicUri){
        internalDecoder = (NimbusJwtDecoder) OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
        internalDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(internalUri));
        // This is not an error!!!
        // We need to init the public decoder from the internal URI - the publicUri might not be reachable by this server!
        publicDecoder = (NimbusJwtDecoder) OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
        publicDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(publicUri));
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            return internalDecoder.decode(token);
        } catch (JwtValidationException e){
            return publicDecoder.decode(token);
        }
    }
}