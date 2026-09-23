package sn.diafoune.allo_dakar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class AlloDakarApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlloDakarApplication.class, args);
    }
}
