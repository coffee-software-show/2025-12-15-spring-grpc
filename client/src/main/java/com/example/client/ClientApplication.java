package com.example.client;

import com.example.client.grpc.GreetingRequest;
import com.example.client.grpc.GreetingServiceGrpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.grpc.client.ChannelBuilderOptions;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.grpc.client.interceptor.security.BearerTokenAuthenticationInterceptor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    private final SecurityContextHolderStrategy strategy =
            SecurityContextHolder.getContextHolderStrategy();


    // todo
    String token(OAuth2AuthorizedClientManager authorizedClientManager) {
        var authenticatedUser = this.strategy.getContext().getAuthentication();
        if (authenticatedUser instanceof OAuth2AuthenticationToken auth2AuthenticationToken) {
            var clientId = auth2AuthenticationToken.getAuthorizedClientRegistrationId();
            var oauthAuthorizationRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId(clientId)
                    .principal(authenticatedUser)
                    .build();
            var authorized = authorizedClientManager.authorize(oauthAuthorizationRequest);
            return Objects.requireNonNull(authorized).getAccessToken().getTokenValue();
        }
        return null;
    }

    @Bean
    @Lazy
    GreetingServiceGrpc.GreetingServiceBlockingStub greetingServiceBlockingStub(
            GrpcChannelFactory channels,
            OAuth2AuthorizedClientManager authorizedClientManager
    ) {
        var bearerTokenInterceptor = new BearerTokenAuthenticationInterceptor(() -> Objects.requireNonNull(
                this.token(authorizedClientManager)));
        var options = ChannelBuilderOptions
                .defaults()
                .withInterceptors(List.of(bearerTokenInterceptor));
        var channel = channels.createChannel("localhost:9090", options);
        return GreetingServiceGrpc.newBlockingStub(channel);
    }
}

@Controller
@ResponseBody
class GreetingClient {

    private final GreetingServiceGrpc.GreetingServiceBlockingStub client;

    GreetingClient(GreetingServiceGrpc.GreetingServiceBlockingStub client) {
        this.client = client;
    }

    @GetMapping("/")
    Map<String, String> message() {
        var request = GreetingRequest
                .newBuilder()
                .setName("Spring fans")
                .build();
        var msg = this.client.greet(request);
        return Map.of("message", msg.getMessage());
    }

}