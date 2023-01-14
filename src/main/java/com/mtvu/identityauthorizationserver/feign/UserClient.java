package com.mtvu.identityauthorizationserver.feign;


import com.mtvu.identityauthorizationserver.config.UserFeignConfiguration;
import com.mtvu.identityauthorizationserver.model.UserLoginType;
import com.mtvu.identityauthorizationserver.record.ChatUserDTO;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-management", url = "${chat.services.user-management-service}",
        configuration = UserFeignConfiguration.class)
@Headers("Authorization: {access_token}")
public interface UserClient {

    @RequestLine("POST /api/user/create/{user_type}")
    ChatUserDTO.Response.Public register(@Param("access_token") String accessToken,
                                         @Param("user_type") UserLoginType userLoginType,
                                         ChatUserDTO.Request.Create userData);

    @RequestLine("GET /api/user/find")
    @Headers({"FindUser: {username}", "FindPwd: {password}"})
    ChatUserDTO.Response.Public findUser(@Param("access_token") String accessToken,
                                         @Param("username") String username,
                                         @Param("password") String password);

    @RequestLine("PUT /api/user/password")
    ChatUserDTO.Response.Public changeUserPassword(@Param("access_token") String accessToken,
                                                   ChatUserDTO.Request.Password password);
}
