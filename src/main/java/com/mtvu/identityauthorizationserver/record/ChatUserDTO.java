package com.mtvu.identityauthorizationserver.record;

import com.mtvu.identityauthorizationserver.model.UserLoginType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public enum ChatUserDTO {;
    public enum Request {;
        public record Create(@NotBlank String userId, String fullName, @NotNull UserLoginType userLoginType,
                             String password, String avatar) {}
    }

    public enum Response {;
        public record Public(@NotBlank String userId, String fullName, @NotNull UserLoginType userLoginType,
                             String avatar, OffsetDateTime createdAt) {}
    }
}
