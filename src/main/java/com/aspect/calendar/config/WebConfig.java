package com.aspect.calendar.config;

import com.aspect.calendar.converters.*;
import com.aspect.calendar.dao.AppConfigDao;
import com.aspect.calendar.entity.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.format.FormatterRegistry;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;


@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AppConfigDao appConfigDao;
    private final Environment environment;

    @Autowired
    WebConfig(AppConfigDao appConfigDao, Environment environment){
        this.appConfigDao = appConfigDao;
        this.environment = environment;
    }

    @Bean
    AppConfig getAppConfig(){
        return this.appConfigDao.getConfig();
    }

    @Bean
    CalendarItemToFormConverter calendarItemToFormConverter(){
        return new CalendarItemToFormConverter();
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(calendarItemToFormConverter());
    }
}
