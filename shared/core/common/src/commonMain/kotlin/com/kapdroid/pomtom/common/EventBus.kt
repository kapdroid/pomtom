package com.kapdroid.pomtom.common

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance

class EventBus {
    private val _events = MutableSharedFlow<DomainEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val events: SharedFlow<DomainEvent> = _events.asSharedFlow()

    suspend fun emit(event: DomainEvent) {
        _events.emit(event)
    }

    fun tryEmit(event: DomainEvent): Boolean = _events.tryEmit(event)

    inline fun <reified T : DomainEvent> subscribe(): SharedFlow<DomainEvent> = events
}

inline fun <reified T : DomainEvent> SharedFlow<DomainEvent>.ofType() = filterIsInstance<T>()
