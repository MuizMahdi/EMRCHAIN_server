package com.project.EhrRoute;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableScheduling
public class EhrRouteApplication
{
    @PostConstruct
    void onInit()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }


    public static void main(String[] args) {
        SpringApplication.run(EhrRouteApplication.class, args);
    }

}