package com.gaguraczi.paw.global.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotEnvConfig {

    /**
     * Loads environment variables from a {@code .env} file in the current directory.
     *
     * @return the loaded environment variables
     */
    @Bean
    public Dotenv dotenv() {
        // .env 파일을 읽어서 환경변수로 사용
        return Dotenv.configure().directory("./")
                .ignoreIfMissing() // .env 파일이 없어도 에러 발생 안함
                .load();
    }
}