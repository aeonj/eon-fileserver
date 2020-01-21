package eon.hg.fileserver.config;

import lombok.Data;

@Data
public class FtpProperties {
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String baseDir;
}
