package dev.rikunode.pupil

import app.morphe.patcher.Fingerprint

/** Exact classes/methods preserved in official Pupil 6.1.1 R8 APK. */
object AnchoredElementCreateFingerprint : Fingerprint(
    definingClass = "Landroidx/compose/foundation/gestures/AnchoredDraggableElement;",
    name = "create",
    parameters = emptyList(),
    returnType = "Landroidx/compose/ui/Modifier\$Node;",
)

object AnchoredElementUpdateFingerprint : Fingerprint(
    definingClass = "Landroidx/compose/foundation/gestures/AnchoredDraggableElement;",
    name = "update",
    parameters = listOf("Landroidx/compose/ui/Modifier\$Node;"),
    returnType = "V",
)
