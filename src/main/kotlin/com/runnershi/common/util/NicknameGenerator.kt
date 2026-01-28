package com.runnershi.common.util

import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class NicknameGenerator {

    companion object {
        private val ADJECTIVES = listOf(
            // 속도/움직임
            "빠른", "날쌘", "달리는", "뛰는", "질주하는",
            // 긍정/에너지
            "열정적인", "씩씩한", "활기찬", "힘찬", "용감한",
            "밝은", "즐거운", "신나는", "행복한", "건강한",
            // 성격/특성
            "멋진", "귀여운", "든든한", "거침없는", "당당한",
            "쿨한", "상큼한", "유쾌한", "명랑한", "활발한",
            // 자연/날씨
            "맑은", "푸른", "산뜻한", "싱그러운", "청량한"
        )

        private val NOUNS = listOf(
            // 동물 (빠른/강한)
            "치타", "호랑이", "표범", "독수리", "매",
            "사자", "늑대", "토끼", "사슴", "말",
            // 동물 (귀여운)
            "돌고래", "펭귄", "코알라", "판다", "여우",
            "고래", "수달", "다람쥐", "부엉이", "참새",
            // 러닝 관련
            "러너", "스프린터", "마라토너", "조깅러", "페이서",
            // 자연
            "바람", "번개", "태풍", "폭풍", "별"
        )
    }

    fun generate(digits: Int = 4): String {
        val adjective = ADJECTIVES.random()
        val noun = NOUNS.random()
        val maxNumber = when (digits) {
            4 -> 10000
            5 -> 100000
            else -> 10000
        }
        val number = Random.nextInt(1, maxNumber).toString().padStart(digits, '0')

        return "$adjective$noun$number"
    }

    fun generateUnique(existsCheck: (String) -> Boolean): String {
        // 4자리 숫자로 최대 10회 시도
        repeat(10) {
            val nickname = generate(digits = 4)
            if (!existsCheck(nickname)) {
                return nickname
            }
        }

        // 10회 실패 시 5자리 숫자로 재시도
        repeat(10) {
            val nickname = generate(digits = 5)
            if (!existsCheck(nickname)) {
                return nickname
            }
        }

        // 그래도 실패 시 타임스탬프 추가
        return "${generate(digits = 5)}${System.currentTimeMillis() % 1000}"
    }
}
