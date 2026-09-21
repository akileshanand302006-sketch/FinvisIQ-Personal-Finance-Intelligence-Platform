package com.smartfinance.backend.config;

import com.smartfinance.backend.security.CurrentUserArgumentResolver;
import com.smartfinance.backend.security.JwtAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;
    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    public WebMvcConfig(JwtAuthInterceptor jwtAuthInterceptor, CurrentUserArgumentResolver currentUserArgumentResolver) {
        this.jwtAuthInterceptor = jwtAuthInterceptor;
        this.currentUserArgumentResolver = currentUserArgumentResolver;
    }

    @Override
    public void addInterceptors(@org.springframework.lang.NonNull InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/health", "/api/health/**", "/api/auth/login", "/api/auth/register");
    }

    @Override
    public void addArgumentResolvers(@org.springframework.lang.NonNull List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }
}
