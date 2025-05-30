package edu.yale.library.paperless.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.retry.annotation.EnableRetry;

// making sure @Transaction has lower precedence,
// see all answers here: https://stackoverflow.com/questions/49678581/spring-retry-with-transactional
@EnableRetry(order = Ordered.LOWEST_PRECEDENCE - 4)
@Configuration
public class ApplicationConfiguration {
}
