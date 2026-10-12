package dev.rikunode.pupil

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

/**
 * Git tags 4.2-beta1 and 4.2-beta2 currently point to the SAME source
 * commit, whose app/build.gradle still declares versionName 4.2-beta1.
 * Both known version strings are advertised, but on-device support is not
 * confirmed. Any future version is explicitly experimental.
 */
val PupilCompatibility = Compatibility(
    name = "Pupil",
    packageName = "xyz.quaver.pupil",
    apkFileType = ApkFileType.APK,
    appIconColor = 0xFFFFFF,
    targets = listOf(
        AppTarget(version = "4.2-beta2"),
        AppTarget(version = "4.2-beta1"),
        AppTarget(version = null, isExperimental = true),
    ),
)
