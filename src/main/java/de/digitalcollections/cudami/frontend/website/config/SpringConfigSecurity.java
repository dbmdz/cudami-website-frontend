package de.digitalcollections.cudami.frontend.website.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

@Configuration
@SuppressFBWarnings(
    value = "THROWS_METHOD_THROWS_CLAUSE_BASIC_EXCEPTION",
    justification = "Spring Security throws java.lang.Exception...")
public class SpringConfigSecurity {

  @Bean
  @Order(SecurityProperties.BASIC_AUTH_ORDER)
  public SecurityFilterChain filterChainMonitoring(HttpSecurity http) throws Exception {
    // Monitoring:
    // https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#actuator.endpoints
    http.authorizeHttpRequests()
        .requestMatchers(EndpointRequest.to(InfoEndpoint.class, HealthEndpoint.class))
        .permitAll()
        .requestMatchers(EndpointRequest.to("prometheus", "version"))
        .permitAll()
        .requestMatchers(EndpointRequest.toAnyEndpoint())
        .hasRole("MONITORING")
        .requestMatchers(AnyRequestMatcher.INSTANCE)
        .permitAll()
        .and()
        .httpBasic();
    return http.build();
  }
}
