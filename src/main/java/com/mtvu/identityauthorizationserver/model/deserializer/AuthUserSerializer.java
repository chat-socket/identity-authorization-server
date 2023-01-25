package com.mtvu.identityauthorizationserver.model.deserializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.mtvu.identityauthorizationserver.model.AuthUser;

import java.io.IOException;


/**
 * Utility class to instruct Jackson module about how to serialise the AuthUser object
 *
 * @author mvu
 */
public class AuthUserSerializer extends StdSerializer<AuthUser> {

    public AuthUserSerializer(Class<AuthUser> t) {
        super(t);
    }

    public AuthUserSerializer() {
        super(AuthUser.class);
    }

    @Override
    public void serialize(AuthUser value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeBooleanField("accountNonLocked", value.isAccountNonLocked());
        gen.writeBooleanField("credentialsNonExpired", value.isCredentialsNonExpired());
        gen.writeBooleanField("accountNonExpired", value.isAccountNonExpired());
        gen.writeBooleanField("enabled", value.isEnabled());

        gen.writeObjectField("claims", value.getClaims());
    }

    @Override
    public void serializeWithType(AuthUser value, JsonGenerator gen,
                                  SerializerProvider provider, TypeSerializer typeSer) throws IOException {

        typeSer.writeTypePrefixForObject(value, gen);
        serialize(value, gen, provider); // call your customized serialize method
        typeSer.writeTypeSuffixForObject(value, gen);
    }
}
