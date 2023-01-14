package com.mtvu.identityauthorizationserver.record;

import com.mtvu.identityauthorizationserver.model.UserLoginType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

/**
 * @author mvu
 * @project chat-socket
 **/
public enum ChatUserDTO {;
    public enum Request {;
        public record Create(@NotBlank String userId, String firstName, String lastName, String password,
                             String avatar) {}

        public record Password(@NotBlank String newPassword) {}
    }

    public enum Response {;
        public record Public(@NotBlank String userId, String firstName, String lastName,
                             @NotNull UserLoginType userLoginType,
                             String avatar, boolean isActivated, boolean isLocked, OffsetDateTime createdAt) {}
    }
}
