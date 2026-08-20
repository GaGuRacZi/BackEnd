package com.gaguraczi.paw.domain.community.fcm;

import com.gaguraczi.paw.domain.community.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Spring 7은 AFTER_COMMIT 리스너에 기본 @Transactional(REQUIRED)을 금지한다.
 * 운영 기동이 이 제약에서 실패한 적이 있어, 컨텍스트 기동으로 막는다.
 */
@SpringJUnitConfig(CommentCreatedListenerContextTest.Config.class)
class CommentCreatedListenerContextTest {

    @Autowired
    private CommentCreatedListener listener;

    @Test
    void 스프링이_리스너_빈을_기동할_수_있다() {
        assertThat(listener).isNotNull();
    }

    @Configuration
    @EnableTransactionManagement
    static class Config {

        @Bean
        CommentCreatedListener commentCreatedListener() {
            return new CommentCreatedListener(mock(CommentRepository.class), mock(CommunityFcmService.class));
        }
    }
}
