package com.example.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Set;

@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @Bean
    Customizer<HttpSecurity> securityCustomizer() {
        return http -> http
                .oauth2AuthorizationServer(a -> a.oidc(Customizer.withDefaults()));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // JdbcUserDetailsManager
    @Bean
    InMemoryUserDetailsManager userDetailsManager(PasswordEncoder passwordEncoder) {
        var users = Set.of(
                User.withUsername("dave")
                        .roles("USER")
                        .password(passwordEncoder.encode("pw"))
                        .build(),
                User.withUsername("chris")
                        .roles("USER")
                        .password(passwordEncoder.encode("pw"))
                        .build(),
                User.withUsername("josh")
                        .roles("USER")
                        .password(passwordEncoder.encode("pw"))
                        .build()
        );
        return new InMemoryUserDetailsManager(users);
    }

}
