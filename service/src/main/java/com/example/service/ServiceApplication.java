package com.example.service;

import com.example.service.grpc.GreetingsServiceGrpc;
import com.example.service.grpc.MessageRequest;
import com.example.service.grpc.MessageResponse;
import io.grpc.stub.StreamObserver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.grpc.server.security.AuthenticationProcessInterceptor;
import org.springframework.grpc.server.security.GrpcSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.stereotype.Service;

@SpringBootApplication
public class ServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }

 /*   @Bean
    JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }*/

    @Bean
    @GlobalServerInterceptor
    AuthenticationProcessInterceptor grpcSecurityFilterChain(GrpcSecurity grpc) throws Exception {
        return grpc
                .authorizeRequests(requests -> requests
                        .methods("GreetingsService/Greetings").authenticated()
                        .methods("grpc.*/*").permitAll()
                        .allRequests().denyAll()
                )
                .oauth2ResourceServer((resourceServer) -> resourceServer.jwt(Customizer.withDefaults()))
                .build();
    }

}

@Service
class GreetingsServiceImpl extends GreetingsServiceGrpc.GreetingsServiceImplBase {

    private final SecurityContextHolderStrategy strategy =
            SecurityContextHolder.getContextHolderStrategy();

    @Override
    public void greetings(MessageRequest request, StreamObserver<MessageResponse> responseObserver) {
        var authentication = this.strategy.getContext().getAuthentication();
        responseObserver
                .onNext(MessageResponse.newBuilder()
                        .setMessage("hello, " + (authentication == null ? "NULL" : authentication.getName()) + "!")
                        .build());
        responseObserver.onCompleted();
    }
}