package com.educagames.api.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.educagames.api.filter.StrictOriginFilter;

/**
 * Configuração de registro global de filtros.
 */
@Configuration
public class FilterConfig {

    /**
     * Registra o filtro de verificação de origem como o primeiro a ser executado.
     *
     * @param filter instância de StrictOriginFilter
     * @return configuração de registro do filtro
     */
    @Bean
    public FilterRegistrationBean<StrictOriginFilter> strictOriginFilterRegistration(StrictOriginFilter filter) {
        FilterRegistrationBean<StrictOriginFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}
