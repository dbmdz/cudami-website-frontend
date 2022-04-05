package de.digitalcollections.cudami.frontend.website.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SpringConfigSecurity extends WebSecurityConfigurerAdapter {
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // Monitoring:
    // https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#actuator.endpoints
    http.antMatcher("/monitoring/**")
        .authorizeRequests()
        .requestMatchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class))
        .permitAll()
        .requestMatchers(EndpointRequest.to("prometheus", "version"))
        .permitAll()
        .requestMatchers(EndpointRequest.toAnyEndpoint())
        .hasRole("ACTUATOR")
        .and()
        .httpBasic();
  }
}
