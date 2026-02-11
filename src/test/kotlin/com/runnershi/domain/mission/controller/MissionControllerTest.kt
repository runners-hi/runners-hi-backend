package com.runnershi.domain.mission.controller

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.common.security.JwtTokenProvider
import com.runnershi.domain.mission.dto.AchievedMissionResponse
import com.runnershi.domain.mission.dto.HomeMissionResponse
import com.runnershi.domain.mission.dto.MissionGroupResponse
import com.runnershi.domain.mission.dto.MissionResponse
import com.runnershi.domain.mission.dto.MyMissionSummaryResponse
import com.runnershi.domain.mission.entity.ConditionType
import com.runnershi.domain.mission.entity.MissionGroupType
import com.runnershi.domain.mission.entity.MissionStatus
import com.runnershi.domain.mission.service.MissionService
import com.runnershi.support.ControllerTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class MissionControllerTest : ControllerTest() {

    @MockitoBean
    private lateinit var missionService: MissionService

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private lateinit var accessToken: String

    @BeforeEach
    fun setUp() {
        accessToken = jwtTokenProvider.createAccessToken(1L)
    }

    @Nested
    @DisplayName("GET /api/missions")
    inner class GetMissionGroups {

        @Test
        @DisplayName("미션 그룹 목록 조회 성공")
        fun success() {
            val missions = listOf(
                MissionResponse(1L, "첫 러닝", null, null, ConditionType.FIRST_RUN, null, null, null, MissionStatus.ACHIEVED, 0)
            )
            val groups = listOf(
                MissionGroupResponse(1L, "웰컴 미션", null, MissionGroupType.WELCOME, null, null, null, missions)
            )
            whenever(missionService.getMissionGroups(any())).thenReturn(groups)

            mockMvc.perform(
                get("/api/missions")
                    .header("Authorization", "Bearer $accessToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("웰컴 미션"))
                .andExpect(jsonPath("$.data[0].missions.length()").value(1))
                .andExpect(jsonPath("$.data[0].missions[0].status").value("ACHIEVED"))
        }

        @Test
        @DisplayName("인증 없이 요청 시 에러 반환")
        fun withoutAuth() {
            mockMvc.perform(get("/api/missions"))
                .andExpect(status().isForbidden)
        }
    }

    @Nested
    @DisplayName("GET /api/missions/home")
    inner class GetHomeMissions {

        @Test
        @DisplayName("홈 미션 조회 성공")
        fun success() {
            val missions = listOf(
                MissionResponse(1L, "할로윈 러닝", null, null, ConditionType.SINGLE_DISTANCE, 5000, null, null, MissionStatus.NOT_ACHIEVED, 0)
            )
            val response = HomeMissionResponse(1L, "할로윈 이벤트", "할로윈 기념 미션", null, null, missions)
            whenever(missionService.getHomeMissions(any())).thenReturn(response)

            mockMvc.perform(
                get("/api/missions/home")
                    .header("Authorization", "Bearer $accessToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.groupName").value("할로윈 이벤트"))
                .andExpect(jsonPath("$.data.missions.length()").value(1))
        }

        @Test
        @DisplayName("활성 홈 미션 없으면 null 반환")
        fun noActiveMission() {
            whenever(missionService.getHomeMissions(any())).thenReturn(null)

            mockMvc.perform(
                get("/api/missions/home")
                    .header("Authorization", "Bearer $accessToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data").isEmpty)
        }
    }

    @Nested
    @DisplayName("GET /api/missions/groups/{groupId}")
    inner class GetMissionGroupDetail {

        @Test
        @DisplayName("미션 그룹 상세 조회 성공")
        fun success() {
            val response = MissionGroupResponse(
                1L, "웰컴 미션", null, MissionGroupType.WELCOME, null, null, null,
                listOf(MissionResponse(1L, "첫 러닝", null, null, ConditionType.FIRST_RUN, null, null, null, MissionStatus.ACHIEVED, 0))
            )
            whenever(missionService.getMissionGroupDetail(any(), any())).thenReturn(response)

            mockMvc.perform(
                get("/api/missions/groups/1")
                    .header("Authorization", "Bearer $accessToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.name").value("웰컴 미션"))
                .andExpect(jsonPath("$.data.missions.length()").value(1))
        }

        @Test
        @DisplayName("존재하지 않는 그룹 - 에러 반환")
        fun notFound() {
            whenever(missionService.getMissionGroupDetail(any(), any()))
                .thenThrow(BusinessException(ErrorCode.MISSION_GROUP_NOT_FOUND))

            mockMvc.perform(
                get("/api/missions/groups/999")
                    .header("Authorization", "Bearer $accessToken")
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.error.code").value("M001"))
        }
    }

    @Nested
    @DisplayName("GET /api/missions/me")
    inner class GetMyMissionSummary {

        @Test
        @DisplayName("내 미션 현황 조회 성공")
        fun success() {
            val response = MyMissionSummaryResponse(
                totalMissions = 10,
                achievedCount = 3,
                inProgressCount = 1,
                recentAchievements = listOf(
                    AchievedMissionResponse(1L, "첫 러닝", null, "웰컴", ConditionType.FIRST_RUN, null)
                )
            )
            whenever(missionService.getMyMissionSummary(any())).thenReturn(response)

            mockMvc.perform(
                get("/api/missions/me")
                    .header("Authorization", "Bearer $accessToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.totalMissions").value(10))
                .andExpect(jsonPath("$.data.achievedCount").value(3))
                .andExpect(jsonPath("$.data.inProgressCount").value(1))
                .andExpect(jsonPath("$.data.recentAchievements.length()").value(1))
        }
    }
}
