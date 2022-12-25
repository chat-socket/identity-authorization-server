package com.mtvu.identityauthorizationserver.record;

import com.mtvu.identityauthorizationserver.model.ChatUser;
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
        public record Create(@NotBlank String userId, String fullName, String password, String avatar) {}
    }

    public enum Response {;
        public record Public(@NotBlank String userId, String fullName, @NotNull UserLoginType userLoginType,
                             String avatar, boolean isActivated, boolean isLocked, OffsetDateTime createdAt) {
            public static ChatUserDTO.Response.Public create(ChatUser chatUser) {
                return new ChatUserDTO.Response.Public(
                        chatUser.getUserId(),
                        chatUser.getFullName(),
                        chatUser.getUserLoginType(),
                        chatUser.getAvatar(),
                        chatUser.isActivated(),
                        chatUser.isLocked(),
                        chatUser.getCreatedAt()
                );
            }
        }
    }
}
