package com.example.client;

import com.example.client.grpc.GreetingsServiceGrpc;
import com.example.client.grpc.MessageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
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


    private final SecurityContextHolderStrategy strategy = SecurityContextHolder.getContextHolderStrategy();

    @Lazy
    @Bean
    GreetingsServiceGrpc.GreetingsServiceBlockingStub greetingsServiceBlockingStub(
            GrpcChannelFactory channels,
            OAuth2AuthorizedClientManager authorizedClientManager) {
        var bearerTokenAuth = new BearerTokenAuthenticationInterceptor(() -> this.token(authorizedClientManager));
        var options = ChannelBuilderOptions.defaults().withInterceptors(List.of(bearerTokenAuth));
        var channel = channels.createChannel("0.0.0.0:9090", options);
        return GreetingsServiceGrpc.newBlockingStub(channel);
    }

    private String token(OAuth2AuthorizedClientManager authorizedClientManager) {
        var auth = this.strategy.getContext().getAuthentication();
        if (auth instanceof OAuth2AuthenticationToken auth2AuthenticationToken) {
            var clientId = auth2AuthenticationToken.getAuthorizedClientRegistrationId();
            var request = OAuth2AuthorizeRequest
                    .withClientRegistrationId(clientId)
                    .principal(auth2AuthenticationToken)
                    .build();
            var authorizedClient = authorizedClientManager.authorize(request);
            return Objects.requireNonNull(authorizedClient).getAccessToken().getTokenValue();
        }
        throw new IllegalStateException("could not install JWT token!");
    }


}

@Controller
@ResponseBody
class GreetingsController {

    private final GreetingsServiceGrpc.GreetingsServiceBlockingStub greetingsServiceBlockingStub;

    private final Logger log = LoggerFactory.getLogger(getClass());

    GreetingsController(GreetingsServiceGrpc.GreetingsServiceBlockingStub greetingsServiceBlockingStub) {
        this.greetingsServiceBlockingStub = greetingsServiceBlockingStub;
    }

    @GetMapping("/")
    Map<String, String> client(@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient client) {
        IO.println("the OAuth 2 client access token is  [" + client.getAccessToken().getTokenValue() + "]");
        var messageRequest = MessageRequest
                .newBuilder()
                .setName("hello")
                .build();
        var hello = this.greetingsServiceBlockingStub.greetings(messageRequest);
        return Map.of("name", hello.getMessage());
    }
}
