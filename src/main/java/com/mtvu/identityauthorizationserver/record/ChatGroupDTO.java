package com.mtvu.identityauthorizationserver.record;

import com.mtvu.identityauthorizationserver.model.ChatGroup;
import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class ChatGroupDTO {
    public enum Response {;
        public record Public(@NotBlank String groupId, String name, String avatar, Set<String> participants,
                             OffsetDateTime createdAt) {
            public static ChatGroupDTO.Response.Public create(ChatGroup chatGroup) {
                var participants = chatGroup.getChatJoinRecords().stream()
                    .map((x) -> x.getChatUser().getUserId())
                    .collect(Collectors.toSet());
                return new ChatGroupDTO.Response.Public(
                    chatGroup.getGroupId(),
                    chatGroup.getGroupName(),
                    chatGroup.getGroupAvatar(),
                    participants,
                    chatGroup.getCreatedAt());
            }
        }
    }
}
