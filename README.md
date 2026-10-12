# Pupil gallery swipe patch

A tiny, independent Morphe-compatible patch for [Pupil](https://github.com/wowososkw/Pupil) (Android, Hitomi.la reader).

**Purpose:** Disable the left-swipe gesture on gallery listing cards, which otherwise reveals download/delete actions. Tapping cards and vertical scrolling should continue to work. This does **not** disable swiping between pages in the reader.

## Install

1. Open [Add this patch source to Morphe](https://morphe.software/add-source?github=rikunode/pupil-swipe-disable-patches), or paste the GitHub repository URL into Morphe Manager's patch sources.
2. Choose Pupil and enable **Disable gallery left swipe**.
3. Patch the original Pupil APK. Make a data backup beforehand; the patched app may need reinstalling due to different app signatures.

Morphe source URL: https://github.com/rikunode/pupil-swipe-disable-patches

## Configuration

- **ON (default):** Enable the patch in Morphe Manager; the card's left-swipe-to-open gesture is disabled in the patched APK.
- **OFF:** Disable the patch and re-patch/reinstall the original app to restore the gesture.

This first version deliberately does *not* add a new runtime preference in Pupil's own settings. That would require separate preference/UI integration and should only be attempted after confirming stability on a real device.

## Implementation

The source matches Pupil's `xyz.quaver.pupil` Android package and `4.2-beta1` repository version. The patch overrides `com.daimajia.swipe.SwipeLayout.isRightSwipeEnabled(): boolean` to return false. In the gallery card XML, `drag_edge="right"`, so a leftward finger swipe opens the right-side action tray. The library's gesture handler calls `isRightSwipeEnabled()` to decide whether to intercept it. Other SwipeLayout edges and click/long-click handling are not modified.

Version compatibility other than 4.2-beta1 is experimental and **has not been verified**. Local build and static checks do not substitute for real Pupil APK patching and on-device testing.

## Build

Derived from [Morphe's official patches template](https://github.com/MorpheApp/morphe-patches-template).

```sh
./gradlew buildAndroid
```

The resulting `.mpp` file is under `patches/build/libs/`. The repository's upstream GitHub Actions workflow produces releases when changes are merged into `main`.

## Source and license

Patch code: GPL-3.0-or-later; [LICENSE](LICENSE). Trademarks and original app copyright belong to their respective owners. This project is independent and not affiliated with Pupil or Morphe.
