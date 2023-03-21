package com.mtvu.identityauthorizationserver.config;

import org.springframework.security.oauth2.jwt.*;

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

    public JwtMultiIssuerDecoder(String internalUri, String publicUri){
        internalDecoder = JwtDecoders.fromIssuerLocation(internalUri);
        // This is not an error!!!
        // We need to init the public decoder from the internal URI - the publicUri might not be reachable by this server!
        publicDecoder = JwtDecoders.fromIssuerLocation(internalUri);
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