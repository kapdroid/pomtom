package com.kapdroid.pomtom.domain.entity

/**
 * The ambient creature shown during FOCUS phases on the session and strict screens.
 *
 * Each entry corresponds 1:1 with a bundled Lottie JSON in the designsystem module
 * (`composeResources/files/companion/<key>.json`). Persistence is by enum [name],
 * so do not rename existing entries — add new ones instead. New values are tolerated
 * defensively on read; unknown values fall back to [Default].
 *
 * [OFF] hides the companion entirely. The picker still shows it as a deliberate choice
 * so users who find any companion distracting can opt out without disabling FOCUS art.
 */
enum class CompanionType {
    OFF,
    MEDITATING_FOX,
    MOON_CAT,
    PLAYFUL_CAT;

    companion object {
        /**
         * Default companion for fresh installs. Picked because the meditating fox is the
         * smallest (~36 KB), reads as the calmest "deep work" pose, and works equally well
         * in either palette.
         */
        val Default: CompanionType = MEDITATING_FOX
    }
}
