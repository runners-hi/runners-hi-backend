package com.runnershi.domain.running.repository

import com.runnershi.domain.running.entity.RunningRecord
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface RunningRecordRepository : JpaRepository<RunningRecord, Long> {

    fun findByUserIdOrderByRunningDateDescCreatedAtDesc(userId: Long, pageable: Pageable): Page<RunningRecord>

    fun findByUserIdAndRunningDate(userId: Long, runningDate: LocalDate): List<RunningRecord>

    @Query("""
        SELECT r FROM RunningRecord r
        WHERE r.userId = :userId
        AND r.runningDate BETWEEN :startDate AND :endDate
        ORDER BY r.runningDate DESC, r.createdAt DESC
    """)
    fun findByUserIdAndDateRange(userId: Long, startDate: LocalDate, endDate: LocalDate): List<RunningRecord>

    @Query("""
        SELECT COALESCE(SUM(r.distance), 0) FROM RunningRecord r
        WHERE r.userId = :userId
    """)
    fun sumDistanceByUserId(userId: Long): Double

    @Query("""
        SELECT COALESCE(SUM(r.distance), 0) FROM RunningRecord r
        WHERE r.userId = :userId
        AND r.runningDate BETWEEN :startDate AND :endDate
    """)
    fun sumDistanceByUserIdAndDateRange(userId: Long, startDate: LocalDate, endDate: LocalDate): Double

    @Query("""
        SELECT COALESCE(SUM(r.duration), 0) FROM RunningRecord r
        WHERE r.userId = :userId
        AND r.runningDate BETWEEN :startDate AND :endDate
    """)
    fun sumDurationByUserIdAndDateRange(userId: Long, startDate: LocalDate, endDate: LocalDate): Int

    @Query("""
        SELECT COUNT(DISTINCT r.runningDate) FROM RunningRecord r
        WHERE r.userId = :userId
        AND r.runningDate BETWEEN :startDate AND :endDate
    """)
    fun countRunningDaysByUserIdAndDateRange(userId: Long, startDate: LocalDate, endDate: LocalDate): Int
}
