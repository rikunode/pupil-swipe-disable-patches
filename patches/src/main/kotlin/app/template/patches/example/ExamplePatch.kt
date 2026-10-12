/*
 * Copyright (C) 2026 rikunode
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Small, narrowly targeted UI patch for Pupil.
 */
package dev.rikunode.pupil

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Pupil's gallery cards use a SwipeLayout with drag_edge="right".
 * A swipe toward the left opens the download/delete tray.
 *
 * Disable only that drag direction: leave the normal click/long-click
 * handlers and RecyclerView vertical scrolling untouched.
 *
 * Morphe Manager's patch enable/disable checkbox is the OFF/ON switch
 * at patch time. This does not add a runtime toggle to Pupil settings.
 */
@Suppress("unused")
val disableGalleryLeftSwipePatch = bytecodePatch(
    name = "Disable gallery left swipe",
    description = "Prevent left swipes on Pupil gallery cards from opening download/delete actions. Regular taps and vertical scrolling remain available.",
    default = true,
) {
    compatibleWith(PupilCompatibility)

    execute {
        RightSwipeEnabledFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """.trimIndent()
        )
    }
}
