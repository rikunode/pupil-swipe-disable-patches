package dev.rikunode.pupil

import app.morphe.patcher.Fingerprint

/**
 * AndroidSwipeLayout's isRightSwipeEnabled() is checked by checkCanDrag()
 * when a gallery card is dragged left. Pupil source uses SwipeLayout 1.2.0.
 * Fingerprint restricts the patch to this precise library method.
 *
 * Patching a library method means a changed/obfuscated app version will
 * fail closed (not silently alter unrelated gesture handling).
 */
object RightSwipeEnabledFingerprint : Fingerprint(
    definingClass = "Lcom/daimajia/swipe/SwipeLayout;",
    name = "isRightSwipeEnabled",
    parameters = emptyList(),
    returnType = "Z",
)
