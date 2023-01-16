package com.mtvu.identityauthorizationserver.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mtvu.identityauthorizationserver.model.deserializer.AuthUserSerializer;
import com.mtvu.identityauthorizationserver.model.deserializer.AuthorityDeserializer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.StandardClaimAccessor;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A customised implementation of the UserDetails interface
 * Which is capable of holding both basic user details to be used with {@AbstractUserDetailsAuthenticationProvider}
 * and Oidc claims to be used with Oidc connect 1.0
 *
 * @author mvu
 */
@JsonSerialize(using = AuthUserSerializer.class)
public class AuthUser implements UserDetails, StandardClaimAccessor {

    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean accountNonExpired;
    private boolean enabled;
    private final Map<String, Object> claims;
    @JsonDeserialize(using = AuthorityDeserializer.class)
    private final List<GrantedAuthority> authorities =
            AuthorityUtils.createAuthorityList("ROLE_USER");


    /**
     * Constructs a {@code AuthUser} using the provided parameters.
     *
     * @param claims the claims about the authentication of the End-User
     */
    public AuthUser(Map<String, Object> claims,
                    boolean enabled,
                    boolean accountNonExpired,
                    boolean credentialsNonExpired,
                    boolean accountNonLocked) {
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.claims = claims;
    }

    public AuthUser() {
        this.claims = new LinkedHashMap<>();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return this.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Map<String, Object> getClaims() {
        return claims;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final Map<String, Object> claims = new LinkedHashMap<>();

        private boolean accountNonLocked = true;
        private boolean credentialsNonExpired = true;
        private boolean accountNonExpired = true;
        private boolean enabled = true;


        private Builder() {
        }

        /**
         * Use this claim in the resulting {@link AuthUser}
         * @param name The claim name
         * @param value The claim value
         * @return the {@link AuthUser.Builder} for further configurations
         */
        public AuthUser.Builder claim(String name, Object value) {
            this.claims.put(name, value);
            return this;
        }

        /**
         * Provides access to every {@link #claim(String, Object)} declared so far with
         * the possibility to add, replace, or remove.
         * @param claimsConsumer the consumer
         * @return the {@link AuthUser.Builder} for further configurations
         */
        public AuthUser.Builder claims(Consumer<Map<String, Object>> claimsConsumer) {
            claimsConsumer.accept(this.claims);
            return this;
        }

        /**
         * Use this address in the resulting {@link AuthUser}
         * @param address The address to use
         * @return the {@link AuthUser.Builder} for further configurations
         */
        public AuthUser.Builder address(String address) {
            return this.claim(StandardClaimNames.ADDRESS, address);
        }

        /**
         * Use this birthdate in the resulting {@link AuthUser}
         * @param birthdate The birthdate to use
         * @return the {@link AuthUser.Builder} for further configurations
         */
        public AuthUser.Builder birthdate(String birthdate) {
            return this.claim(StandardClaimNames.BIRTHDATE, birthdate);
        }

        /**
         * Use this email in the resulting {@link AuthUser}
         * @param email The email to use
         * @return the {@link AuthUser.Builder} for further configurations
         */
        public AuthUser.Builder email(String email) {
            return this.claim(StandardClaimNames.EMAIL, email);
        }

        /**
         * Use this verified-email indicator in the resulting {@link AuthUser}
         * @param emailVerified The verified-email indicator to use
         * @return the {@link AuthUser.Builder} for further configurations
         */
        public AuthUser.Builder emailVerified(Boolean emailVerified) {
            return this.claim(StandardClaimNames.EMAIL_VERIFIED, emailVerified);
        }

        /**
         * Use this family name in the resulting {@link AuthUser}
         * @param familyName The family name to use
         * @return the {@link AuthUser.Builder} for further configurations
         */
        public AuthUser.Builder familyName(String familyName) {
            return claim(StandardClaimNames.FAMILY_NAME, familyName);
        }

        /**
         * Use this gender in the resulting {@link AuthUser}
         * @param gender The gender to use
         * @return the {@link AuthUser.Builder} for further configurations
         */
        public AuthUser.Builder gender(String gender) {
            return this.claim(StandardClaimNames.GENDER, gender);
        }

        /**
         * Use this given name in the resulting {@link AuthUser}
         * @param givenName The given name to use
         * @return the {@link AuthUser.Builder} for further configurations
         */
        public AuthUser.Builder givenName(String givenName) {
            return claim(StandardClaimNames.GIVEN_NAME, givenName);
        }

        /**
         * Use this locale in the resulting {@link AuthUser}
         * @param locale The locale to use
         * @return the {@link AuthUser.Builder} for further configurations
         */
        public AuthUser.Builder locale(String locale) {
            return this.claim(StandardClaimNames.LOCALE, locale);
        }

        /**
         * Use this middle name in the resulting {@link AuthUser}
         * @param middleName The middle name to use
         * @return the {@link AuthUser.Builder} for further configurations
         */
        public AuthUser.Builder middleName(String middleName) {
            return claim(StandardClaimNames.MIDDLE_NAME, middleName);
        }

        /**
         * Use this name in the resulting {@link AuthUser}
         * @param name The name to use
         * @return the {@link AuthUser.Builder} for further configurations
         */
        public AuthUser.Builder name(String name) {
            return claim(StandardClaimNames.NAME, name);
        }

        /**
         * Use this nickname in the resulting {@link AuthUser}
         * @param nickname The nickname to use
         * @return the {@link AuthUser.Builder} for further configurations
         */
        public AuthUser.Builder nickname(String nickname) {
            return claim(StandardClaimNames.NICKNAME, nickname);
        }

        /**
         * Use this picture in the resulting {@link AuthUser}
         * @param picture The picture to use
         * @return the {@link AuthUser.Builder} for further configurations
         */
        public AuthUser.Builder picture(String picture) {
            return this.claim(StandardClaimNames.PICTURE, picture);
        }

        /**
         * Use this phone number in the resulting {@link AuthUser}
         * @param phoneNumber The phone number to use
         * @return the {@link AuthUser.Builder} for further configurations
         */
        public AuthUser.Builder phoneNumber(String phoneNumber) {
            return this.claim(StandardClaimNames.PHONE_NUMBER, phoneNumber);
        }

        /**
         * Use this verified-phone-number indicator in the resulting {@link AuthUser}
         * @param phoneNumberVerified The verified-phone-number indicator to use
         * @return the {@link AuthUser.Builder} for further configurations
         * @since 5.8
         */
        public AuthUser.Builder phoneNumberVerified(Boolean phoneNumberVerified) {
            return this.claim(StandardClaimNames.PHONE_NUMBER_VERIFIED, phoneNumberVerified);
        }

        /**
         * Use this preferred username in the resulting {@link AuthUser}
         * @param preferredUsername The preferred username to use
         * @return the {@link AuthUser.Builder} for further configurations
         */
        public AuthUser.Builder preferredUsername(String preferredUsername) {
            return claim(StandardClaimNames.PREFERRED_USERNAME, preferredUsername);
        }

        /**
         * Use this profile in the resulting {@link AuthUser}
         * @param profile The profile to use
         * @return the {@link AuthUser.Builder} for further configurations
         */
        public AuthUser.Builder profile(String profile) {
            return claim(StandardClaimNames.PROFILE, profile);
        }

        /**
         * Use this subject in the resulting {@link AuthUser}
         * @param subject The subject to use
         * @return the {@link AuthUser.Builder} for further configurations
         */
        public AuthUser.Builder subject(String subject) {
            return this.claim(StandardClaimNames.SUB, subject);
        }

        /**
         * Use this updated-at {@link Instant} in the resulting {@link AuthUser}
         * @param updatedAt The updated-at {@link Instant} to use
         * @return the {@link AuthUser.Builder} for further configurations
         */
        public AuthUser.Builder updatedAt(String updatedAt) {
            return this.claim(StandardClaimNames.UPDATED_AT, updatedAt);
        }

        /**
         * Use this website in the resulting {@link AuthUser}
         * @param website The website to use
         * @return the {@link AuthUser.Builder} for further configurations
         */
        public AuthUser.Builder website(String website) {
            return this.claim(StandardClaimNames.WEBSITE, website);
        }

        /**
         * Use this zoneinfo in the resulting {@link AuthUser}
         * @param zoneinfo The zoneinfo to use
         * @return the {@link AuthUser.Builder} for further configurations
         */
        public AuthUser.Builder zoneinfo(String zoneinfo) {
            return this.claim(StandardClaimNames.ZONEINFO, zoneinfo);
        }

        public AuthUser.Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public AuthUser.Builder accountNonLocked(boolean accountNonLocked) {
            this.accountNonLocked = accountNonLocked;
            return this;
        }

        public AuthUser.Builder credentialsNonExpired(boolean credentialsNonExpired) {
            this.credentialsNonExpired = credentialsNonExpired;
            return this;
        }

        public AuthUser.Builder accountNonExpired(boolean accountNonExpired) {
            this.accountNonExpired = accountNonExpired;
            return this;
        }

        /**
         * Build the {@link AuthUser}
         * @return The constructed {@link AuthUser}
         */
        public AuthUser build() {
            return new AuthUser(this.claims, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked);
        }

    }
}
