package com.cloud;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@EnableSwagger2
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
@MapperScan("com.cloud.modules.**.mapper")
public class  MicroSystemApplication {

  public static void main(String[] args) throws UnknownHostException {

    ConfigurableApplicationContext application = SpringApplication.run(MicroSystemApplication.class, args);
    Environment env = application.getEnvironment();
    String ip = InetAddress.getLocalHost().getHostAddress();
    String port = env.getProperty("server.port");
    log.info("\n----------------------------------------------------------\n\t" +
        "Application Micro-Cloud is running! Access URLs:\n\t" +
        "Local: \t\thttp://localhost:" + port  + "/\n\t" +
        "External: \thttp://" + ip + ":" + port  + "/\n\t" +
        "Swagger-UI: \t\thttp://" + ip + ":" + port  + "/doc.html\n" +
        "---------------------------系统服务启动成功-----------------------");
  }
}