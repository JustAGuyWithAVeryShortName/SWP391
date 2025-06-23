package com.swp2.demo.security;

import com.swp2.demo.Repository.UserRepository;
import com.swp2.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // <-- cần import để phân biệt GET/POST
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
        provider.setPasswordEncoder(NoOpPasswordEncoder.getInstance()); // hoặc dùng passwordEncoder()
        return provider;
    }

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ✅ Nhớ URL trước khi login
                .addFilterBefore(new UrlMemoryFilter(), UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/home", "/register/**", "/login", "/css/**", "/images/**", "/js/**",
                                "/member", "/forgot-password", "/reset-password",
                                "/ws/**","/about_us"
                        ).permitAll()

                        // ✅ Cho phép submit POST /questionnaire không cần login
                        .requestMatchers(HttpMethod.POST, "/questionnaire").permitAll()

                        // ✅ Nhưng GET /questionnaire thì cần đăng nhập
                        .requestMatchers(HttpMethod.GET, "/questionnaire").authenticated()

                        .requestMatchers("/admin/**").hasAuthority("Admin")
                        .requestMatchers("/profile").authenticated()
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
                );

        return http.build();
    }
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            com.swp2.demo.entity.User user = userRepository.findByUsername(username); // Get the User object directly

            if (user == null) { // Check if the user was found
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
