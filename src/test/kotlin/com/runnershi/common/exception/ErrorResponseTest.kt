package com.runnershi.common.exception

import com.runnershi.support.ControllerTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ErrorResponseTest : ControllerTest() {

    @Test
    @DisplayName("없는 주소는 404 JSON")
    fun unknownRoute() {
        mockMvc.perform(get("/nope"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("C003"))
            .andExpect(jsonPath("$.error.message").value("존재하지 않는 주소입니다"))
    }

    @Test
    @DisplayName("토큰 없이 보호된 API는 401 JSON")
    fun unauthenticated() {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("A003"))
    }

    @Test
    @DisplayName("깨진 JSON 본문은 400 JSON")
    fun malformedBody() {
        mockMvc.perform(post("/api/auth/kakao").contentType(MediaType.APPLICATION_JSON).content("x"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error.code").value("C001"))
    }

    @Test
    @DisplayName("허용되지 않은 메서드는 405 JSON")
    fun methodNotAllowed() {
        mockMvc.perform(put("/api/terms"))
            .andExpect(jsonPath("$.success").value(false))
    }
}
