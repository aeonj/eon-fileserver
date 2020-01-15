package eon.hg.fileserver.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 自定义属性配置
 * @author eonook
 */
@Configuration
@EnableConfigurationProperties(FileServerProperties.class)
public class PropertiesConfig {
}
