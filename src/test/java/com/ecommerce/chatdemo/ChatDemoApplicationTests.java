package com.ecommerce.chatdemo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Disabled("CI에서는 전체 컨텍스트 로딩 테스트 제외")
@SpringBootTest
@ActiveProfiles("test")
class ChatDemoApplicationTests {

    @Test
    void contextLoads() {
    }
}
