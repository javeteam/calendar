package com.aspect.calendar.config;

import com.aspect.calendar.dao.AppConfigDao;
import com.aspect.calendar.entity.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AppConfigDao appConfigDao;

    @Autowired
    WebConfig(AppConfigDao appConfigDao){
        this.appConfigDao = appConfigDao;
    }

    @Bean
    AppConfig getAppConfig(){
        return this.appConfigDao.getConfig();
    }
}
