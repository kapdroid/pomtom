package com.kapdroid.pomtom.database.internal

import com.kapdroid.pomtom.database.Custom_audio_track
import com.kapdroid.pomtom.database.Focus_session
import com.kapdroid.pomtom.database.Goal as DbGoal
import com.kapdroid.pomtom.database.Wallpaper as DbWallpaper
import com.kapdroid.pomtom.domain.entity.AudioCategory
import com.kapdroid.pomtom.domain.entity.AudioSourceKind
import com.kapdroid.pomtom.domain.entity.AudioTrack
import com.kapdroid.pomtom.domain.entity.FocusSession
import com.kapdroid.pomtom.domain.entity.Goal
import com.kapdroid.pomtom.domain.entity.GoalAttachMode
import com.kapdroid.pomtom.domain.entity.GoalColor
import com.kapdroid.pomtom.domain.entity.GoalType
import com.kapdroid.pomtom.domain.entity.SessionPhase
import com.kapdroid.pomtom.domain.entity.SessionStatus
import com.kapdroid.pomtom.domain.entity.Wallpaper
import com.kapdroid.pomtom.domain.entity.WallpaperSource

internal fun DbGoal.toDomain(): Goal = Goal(
    id = id,
    title = title,
    type = enumValueOf<GoalType>(type),
    target = target.toInt(),
    progress = progress.toInt(),
    attachMode = enumValueOf<GoalAttachMode>(attach_mode),
    color = enumValueOf<GoalColor>(color),
    createdAtMs = created_at,
    completedAtMs = completed_at,
)

internal fun Focus_session.toDomain(): FocusSession = FocusSession(
    id = id,
    goalId = goal_id,
    startedAtMs = started_at,
    resumedAtMs = resumed_at,
    endedAtMs = ended_at,
    plannedMs = planned_ms,
    actualMs = actual_ms,
    phase = enumValueOf<SessionPhase>(phase),
    cycleIndex = cycle_index.toInt(),
    status = enumValueOf<SessionStatus>(status),
    strictMode = strict_mode == 1L,
)

internal fun DbWallpaper.toDomain(): Wallpaper = Wallpaper(
    id = id,
    displayName = display_name,
    localPath = local_path,
    source = enumValueOf<WallpaperSource>(source),
    addedAtMs = added_at,
)

internal fun Custom_audio_track.toAudioTrack(): AudioTrack = AudioTrack(
    id = id,
    displayName = display_name,
    category = AudioCategory.AMBIENT,
    source = AudioSourceKind.CUSTOM,
    resourcePath = null,
    localPath = local_path,
    durationMs = duration_ms,
    sizeBytes = size_bytes,
    addedAtMs = added_at,
)
