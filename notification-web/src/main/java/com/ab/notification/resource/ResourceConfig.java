package com.ab.notification.resource;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * It is used to register resource interceptor for logging system resources and time taken by API execution.
 */
@Configuration
public class ResourceConfig implements WebMvcConfigurer {

    @Autowired
    private ResourceInterceptor resourceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(resourceInterceptor);
    }

}
