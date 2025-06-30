package com.delphi.delphi.configs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.delphi.delphi.components.JwtService;
import com.delphi.delphi.filters.JwtAuthFilter;
import com.delphi.delphi.repositories.UserRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
 /*
 * TODO: add Single sign on
 * TODO: add 2FA
 */
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        return new JwtAuthFilter(jwtService, userDetailsService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
            .cors(Customizer.withDefaults()) // enables CORS using the bean defined in CorsConfig
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // @Bean
    // public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
    //     http
    //     .cors(Customizer.withDefaults()) // enables CORS using the bean defined in CorsConfig
    //     .csrf(csrf -> csrf.disable()) // CSRF protection
    //     .authorizeHttpRequests(auth -> auth
    //     .requestMatchers("/", "/error", "/webjars/**", "/ws/**", "/sockjs/**").permitAll() // these URLs don't require authentication
    //     .anyRequest().authenticated()) // anything else requires authentication

    //     .oauth2Login(oauth2login -> oauth2login
    //     .successHandler((request, response, authentication) -> response.sendRedirect("/auth/details")));


    //     // Logout user
    //     // .logout(logout -> logout
    //     //     .logoutSuccessUrl("/logout-success") // Redirect to a success page after logout
    //     //     .clearAuthentication(true)
    //     //     .deleteCookies("JSESSIONID")
    //     //     .logoutUrl("/logout") // Specify the logout URL
    //         // .addLogoutHandler((request, response, authentication) -> {
    //         //     // Custom logic to handle OAuth logout
    //         //     // Redirect to the OAuth provider's logout URL
    //         //     String oauthLogoutUrl = "https://oauth-provider.com/logout"; // Replace with actual logout URL
    //         //     try {
    //         //         response.sendRedirect(oauthLogoutUrl);
    //         //     } catch (IOException e) {
    //         //         // TODO Auto-generated catch block
    //         //         System.out.println("Error logging out: " + e.getMessage());
    //         //     }
    //         // })
    //     //);
        
    //     // Exception handling -- redirect user to login page
    //     // .exceptionHandling(exception -> exception
    //     // .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

    //     // Handle unauthorized access
    //     // .exceptionHandling(exception -> exception
    //     // .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
    //     // );
        
    //     return http.build();
    // }
    

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username).orElseThrow();
    }
}

