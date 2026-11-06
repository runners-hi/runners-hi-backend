package com.runnershi.dev

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** dev 프로필이 아니면 테스트 로그인·시더가 만들어지지 않는다(상용 보호). */
@SpringBootTest
@ActiveProfiles("test")
class DevProfileGateTest {

    @Autowired
    lateinit var ctx: ApplicationContext

    @Test
    @DisplayName("test 프로필에는 dev 빈이 없다")
    fun noDevBeans() {
        assertEquals(0, ctx.getBeansOfType(DevAuthController::class.java).size)
        assertEquals(0, ctx.getBeansOfType(DevDataSeeder::class.java).size)
    }
}

@SpringBootTest(properties = ["spring.jpa.properties.hibernate.default_schema=PUBLIC"])
@ActiveProfiles("dev", "test")
class DevProfileEnabledTest {

    @Autowired
    lateinit var ctx: ApplicationContext

    @Test
    @DisplayName("dev 프로필에는 dev 빈이 있다")
    fun devBeans() {
        assertTrue(ctx.getBeansOfType(DevAuthController::class.java).isNotEmpty())
        assertTrue(ctx.getBeansOfType(DevDataSeeder::class.java).isNotEmpty())
    }
}
