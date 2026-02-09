package com.runnershi.domain.app.repository

import com.runnershi.domain.app.entity.AppVersion
import com.runnershi.domain.app.entity.Platform
import org.springframework.data.jpa.repository.JpaRepository

interface AppVersionRepository : JpaRepository<AppVersion, Long> {

    fun findByPlatform(platform: Platform): AppVersion?
}
