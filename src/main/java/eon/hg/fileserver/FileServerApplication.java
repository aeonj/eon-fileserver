package eon.hg.fileserver;

import com.github.tobato.fastdfs.FdfsClientConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.Import;
import org.springframework.jmx.support.RegistrationPolicy;

@SpringBootApplication
@Import(FdfsClientConfig.class)
@MapperScan("eon.hg.fileserver.mapper")
//解决jmx注册冲突
@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
public class FileServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileServerApplication.class, args);
    }

}
