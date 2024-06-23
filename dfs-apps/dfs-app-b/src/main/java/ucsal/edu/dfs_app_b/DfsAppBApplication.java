package ucsal.edu.dfs_app_b;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = "ucsal.edu")
public class DfsAppBApplication {

	public static void main(String[] args) {
		SpringApplication.run(DfsAppBApplication.class, args);
	}

}
