package eon.hg.fileserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 自定义文件服务器属性注入
 * @author eonook
 */

@ConfigurationProperties(prefix = "eon.hg.file-server")
@Data
public class FileServerProperties {
    private String cacheType;
    private String uploadFolder;
    private String serverUrl;
    private FtpProperties ftp;
}
