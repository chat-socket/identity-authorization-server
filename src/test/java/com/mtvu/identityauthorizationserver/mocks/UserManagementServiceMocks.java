package com.mtvu.identityauthorizationserver.mocks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.mtvu.identityauthorizationserver.model.UserLoginType;
import com.mtvu.identityauthorizationserver.record.ChatUserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.OffsetDateTime;

public class UserManagementServiceMocks {

    private static ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    public static void setupMockBooksResponse(WireMockServer mockService) throws IOException {
        var user = new ChatUserDTO.Response.Public("user1", "Vu Manh Tu", UserLoginType.EMAIL,
                "", true, false, OffsetDateTime.now());
        mockService.stubFor(WireMock.get(WireMock.urlEqualTo("/api/user/find"))
                        .withHeader("FindUser", WireMock.equalTo("user1"))
                        .withHeader("FindPwd", WireMock.equalTo("password"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(user))));
    }
}
