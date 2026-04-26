package com.kapdroid.pomtom.common.di

import com.kapdroid.pomtom.common.Clock
import com.kapdroid.pomtom.common.EventBus
import com.kapdroid.pomtom.common.IdGenerator
import com.kapdroid.pomtom.common.SystemClock
import com.kapdroid.pomtom.common.UuidIdGenerator
import org.koin.dsl.module

val commonModule = module {
    single<Clock> { SystemClock() }
    single<IdGenerator> { UuidIdGenerator() }
    single { EventBus() }
}
