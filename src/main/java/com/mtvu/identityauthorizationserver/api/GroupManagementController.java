package com.mtvu.identityauthorizationserver.api;

import com.mtvu.identityauthorizationserver.record.ChatGroupRecord;
import com.mtvu.identityauthorizationserver.service.ChatGroupService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.notFound;

@AllArgsConstructor
@RestController("/group")
public class GroupManagementController {

    private final ChatGroupService chatGroupService;

    @GetMapping("/{groupId}")
    public ResponseEntity<ChatGroupRecord> getGroup(@PathVariable("groupId") String groupId) {
        var group = chatGroupService.getChatGroup(groupId);
        return group.map((x) -> ResponseEntity.ok(new ChatGroupRecord(x))).orElseGet(() -> notFound().build());
    }
}