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
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
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
        provider.setPasswordEncoder(NoOpPasswordEncoder.getInstance()); // Consider changing to passwordEncoder()
        return provider;
    }

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Autowired
    private UserRepository userRepository;

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler("/error");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(new UrlMemoryFilter(), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // 1. ABSOLUTELY PUBLICLY ACCESSIBLE PATHS
                // Define public paths that ALL users (including Admin) can access.
                // It's crucial that these match the 'isPublicPath' logic in role-specific access rules.
                .requestMatchers(
                    "/", "/home", "/register/**", "/login", "/css/**", "/images/**", "/js/**",
                    "/member", "/forgot-password", "/reset-password",
                    "/ws/**", "/about_us", "/.well-known/**",
                    "/spring-security-logout",
                    "/error"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/questionnaire").permitAll()

                // 2. ADMIN SPECIFIC RULES: ADMINS CAN ONLY ACCESS /admin/** and defined public paths
                // This rule must come BEFORE any general authenticated() or other role-specific rules
                .requestMatchers("/admin/**").hasAuthority("Admin") // Admins can access all admin paths
                .requestMatchers("/**").access((authenticationSupplier, context) -> {
                    Authentication authentication = authenticationSupplier.get();
                    HttpServletRequest request = context.getRequest();
                    String requestUri = request.getRequestURI();

                    boolean isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("Admin"));

                    if (isAdmin) {
                        // Define what an Admin IS allowed to access (admin paths + public paths)
                        boolean isPublicPath = requestUri.startsWith("/") ||
                            requestUri.startsWith("/home") ||
                            requestUri.startsWith("/register") ||
                            requestUri.startsWith("/login") ||
                            requestUri.startsWith("/css/") ||
                            requestUri.startsWith("/images/") ||
                            requestUri.startsWith("/js/") ||
                            requestUri.startsWith("/member") ||
                            requestUri.startsWith("/forgot-password") ||
                            requestUri.startsWith("/reset-password") ||
                            requestUri.startsWith("/ws/") ||
                            requestUri.startsWith("/about_us") ||
                            requestUri.startsWith("/.well-known/") ||
                            requestUri.startsWith("/spring-security-logout") ||
                            requestUri.startsWith("/error");

                        // Admin is ALLOWED only if it's an admin path OR a public path
                        // If it's an admin and it's NOT an admin path AND NOT a public path, then DENY
                        boolean isAllowedForAdmin = requestUri.startsWith("/admin") || isPublicPath;
                        return new AuthorizationDecision(isAllowedForAdmin);
                    }
                    // If it's NOT an Admin, this rule doesn't apply to them, let other rules handle.
                    return new AuthorizationDecision(true);
                })

                // 3. PATHS ACCESSIBLE BY BOTH COACH AND MEMBER (excluding Admin)
                .requestMatchers(
                    "/users",
                    "/api/users/{userId}/profile",
                    "/api/users/{userId}/latest-survey-answer",
                    "/api/users/{userId}/latest-quit-plan",
                    "/profile",
                    "/questionnaire"
                ).hasAnyAuthority("Coach", "Member")

                // NEW RULE FOR CHAT PAGES: Only Coach or PREMIUM Member can access
                .requestMatchers(
                    "/messenger",
                    "/messages/**"
                ).access((authenticationSupplier, context) -> {
                    Authentication authentication = authenticationSupplier.get();
                    String username = authentication.getName();
                    User user = userRepository.findByUsername(username);

                    boolean isCoach = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("Coach"));
                    boolean isPremiumMember = false;

                    if (user != null && user.getRole().name().equals("Member") && user.getMember() != null) {
                        isPremiumMember = user.getMember().equals(Member.PREMIUM);
                    }

                    // Only Coach OR Premium Member can access these
                    return new AuthorizationDecision(isCoach || isPremiumMember);
                })

                // 4. DENY ALL OTHER ACCESS FOR COACHES AND MEMBERS (General catch-all for them)
                // This rule acts as a catch-all *only for Coaches and Members* for paths not explicitly allowed above.
                // It must come *before* anyRequest().authenticated()
                .requestMatchers("/**").access((authenticationSupplier, context) -> {
                    Authentication authentication = authenticationSupplier.get();
                    HttpServletRequest request = context.getRequest();
                    String requestUri = request.getRequestURI();

                    boolean isCoach = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("Coach"));
                    boolean isMember = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("Member"));

                    if (isCoach || isMember) { // Apply this custom access logic to both Coach and Member
                        // List *all* paths that a Coach or Member is explicitly permitted to access.
                        boolean isAllowed = requestUri.startsWith("/") ||
                            requestUri.startsWith("/home") ||
                            requestUri.startsWith("/register") ||
                            requestUri.startsWith("/login") ||
                            requestUri.startsWith("/css/") ||
                            requestUri.startsWith("/images/") ||
                            requestUri.startsWith("/js/") ||
                            requestUri.startsWith("/member") ||
                            requestUri.startsWith("/forgot-password") ||
                            requestUri.startsWith("/reset-password") ||
                            requestUri.startsWith("/ws/") ||
                            requestUri.startsWith("/about_us") ||
                            requestUri.startsWith("/.well-known/") ||
                            requestUri.startsWith("/spring-security-logout") ||
                            requestUri.startsWith("/questionnaire") ||
                            requestUri.startsWith("/users") ||
                            requestUri.startsWith("/api/users/") ||
                            requestUri.startsWith("/profile") ||
                            requestUri.startsWith("/messenger") ||
                            requestUri.startsWith("/messages") ||
                            requestUri.startsWith("/error");

                        // If the Coach/Member tries to access a path NOT in their allowed list, deny it.
                        return new AuthorizationDecision(isAllowed);
                    }
                    // If it's NOT a Coach or Member, this rule doesn't deny them.
                    // Their access is determined by subsequent rules (like anyRequest().authenticated()).
                    return new AuthorizationDecision(true);
                })


                // 5. FALLBACK: Any other request not caught by specific rules, requires authentication.
                // This will catch any authenticated user (e.g., Admin who falls through, or a role not explicitly handled)
                // and deny them if they haven't been explicitly allowed by earlier rules.
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
            )

            .exceptionHandling(exceptions -> exceptions
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