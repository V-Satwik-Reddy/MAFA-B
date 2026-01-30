package majorproject.maf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
//@SpringBootApplication(exclude = {
//        org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class,
//        org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration.class
//})
public class MafApplication {

    public static void main(String[] args) {
        SpringApplication.run(MafApplication.class, args);
    }

}
