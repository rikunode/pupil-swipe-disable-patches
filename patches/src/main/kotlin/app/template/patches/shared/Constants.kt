package dev.rikunode.pupil

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

/**
 * Version 4.2-beta1 comes from wowososkw/Pupil app/build.gradle.
 * Other versions are explicitly experimental: a matching fingerprint and
 * an actual device check are required before claiming compatibility.
 */
val PupilCompatibility = Compatibility(
    name = "Pupil",
    packageName = "xyz.quaver.pupil",
    apkFileType = ApkFileType.APK,
    appIconColor = 0xFFFFFF,
    targets = listOf(
        AppTarget(version = "4.2-beta1"),
        AppTarget(version = null, isExperimental = true),
    ),
)
