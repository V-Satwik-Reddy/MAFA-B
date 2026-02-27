package majorproject.maf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // The number of threads always kept alive
        executor.setCorePoolSize(10);
        // Maximum number of threads if the queue fills up
        executor.setMaxPoolSize(10);
        // The number of tasks that can wait in the queue before new threads are created
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("AlertWorker-");
        executor.initialize();
        return executor;
    }
}