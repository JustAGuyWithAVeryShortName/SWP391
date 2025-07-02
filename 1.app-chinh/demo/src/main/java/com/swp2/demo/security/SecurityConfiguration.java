package com.swp2.demo.security;

import com.swp2.demo.Repository.UserRepository;
import com.swp2.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder; // Consider changing this!
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.function.Supplier;

import com.swp2.demo.entity.Member;
import com.swp2.demo.entity.User;

@Configuration
public class SecurityConfiguration {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserService userService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        // WARNING: NoOpPasswordEncoder is NOT secure for production.
        // It's highly recommended to use passwordEncoder() here.
        provider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
        // For production, change to:
        // provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Autowired
    private UserRepository userRepository;

    // We still define this, but its primary role will be to re-throw or let the exception propagate
    // so the @ControllerAdvice can catch it.
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        // You can leave the error page path, but it will mostly be handled by GlobalExceptionHandler
        return new CustomAccessDeniedHandler("/error");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(new UrlMemoryFilter(), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // 1. ABSOLUTELY PUBLICLY ACCESSIBLE PATHS (PermitAll for everyone, including Admins)
                .requestMatchers(
                    "/", "/home", "/register/**", "/login", "/css/**", "/images/**", "/js/**",
                    "/member", "/forgot-password", "/reset-password",
                    "/ws/**", "/about_us", "/.well-known/**",
                    "/spring-security-logout", // <-- Ensure this is permitted for ALL, including anonymous for proper logout flow
                    "/error" // <-- Your custom error page
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/questionnaire").permitAll()

                // 2. ADMIN SPECIFIC RULE (Highest priority for security-sensitive admin paths)
                .requestMatchers("/admin/**").hasAuthority("Admin")

                // 3. CHAT PAGES: Only Coach or PREMIUM Member can access
                .requestMatchers(
                    "/messenger",
                    "/messages/**"
                ).access((authenticationSupplier, context) -> {
                    Authentication authentication = authenticationSupplier.get();
                    // If not authenticated, deny immediately.
                    if (!authentication.isAuthenticated()) {
                        return new AuthorizationDecision(false);
                    }
                    String username = authentication.getName();
                    User user = userRepository.findByUsername(username);

                    boolean isCoach = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("Coach"));
                    boolean isPremiumMember = false;

                    if (user != null && user.getRole().name().equals("Member") && user.getMember() != null) {
                        // Assuming Member.PREMIUM is an enum value or static final field.
                        // Corrected comparison:
                        isPremiumMember = "PREMIUM".equals(user.getMember().toString()); // Use .name() or .toString() for enum comparison
                    }

                    return new AuthorizationDecision(isCoach || isPremiumMember);
                })

                // 4. GENERAL RULES FOR AUTHENTICATED USERS (Coach, Member, etc.)
                // These apply to users who are logged in but are not "Admin" for /admin/** paths.
                .requestMatchers(
                    "/users",
                    "/api/users/{userId}/profile",
                    "/api/users/{userId}/latest-survey-answer",
                    "/api/users/{userId}/latest-quit-plan",
                    "/profile",
                    "/questionnaire"
                ).hasAnyAuthority("Coach", "Member")

                // 5. CATCH-ALL FOR AUTHENTICATED USERS:
                // Any other request not explicitly permitted or denied above,
                // if the user is authenticated, check their role.
                // This means an Admin will access anything if not caught by a specific deny.
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/authenticateTheUser")
                .successHandler(customAuthenticationSuccessHandler)
                .permitAll()
            )

            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .successHandler(customAuthenticationSuccessHandler)
            )

            .logout(logout -> logout
                .logoutUrl("/spring-security-logout")
                .logoutSuccessUrl("/home")
                .permitAll() // Ensure logout is accessible to all, including pre-logout state
            )

            .exceptionHandling(exceptions -> exceptions
                // The accessDeniedHandler will primarily re-throw the AccessDeniedException
                // which will then be caught by the @ControllerAdvice.
                .accessDeniedHandler(accessDeniedHandler())
            );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            com.swp2.demo.entity.User user = userRepository.findByUsername(username);

            if (user == null) {
                throw new UsernameNotFoundException("User not found: " + username);
            }

            return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
        };
    }
}