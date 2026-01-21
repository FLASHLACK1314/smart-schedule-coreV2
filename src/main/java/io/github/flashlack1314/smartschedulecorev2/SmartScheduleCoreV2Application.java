package io.github.flashlack1314.smartschedulecorev2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 智能排课核心服务V2启动类
 * @author flash
 */
@SpringBootApplication
@EnableConfigurationProperties
public class SmartScheduleCoreV2Application {

    public static void main(String[] args) {
        SpringApplication.run(SmartScheduleCoreV2Application.class, args);
    }

}
