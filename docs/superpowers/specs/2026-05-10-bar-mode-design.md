# Bar Mode Design Spec

## Overview

Add top bar COLLAPSED mode (from Tomato) and bottom bar FLOATING mode (from June).
Both modes are stored in Theme, changed via theme settings page.

## New Enums

### TopBarMode (core/designsystem/theme/TopBarMode.kt)

- PINNED: current CenterAlignedTopAppBar, fixed
- COLLAPSED: TopAppBar + exitUntilCollapsedScrollBehavior() + 32sp title, collapses on scroll

### BottomBarMode (core/designsystem/theme/BottomBarMode.kt)

- STANDARD: current NavigationBar, occupies bottom space
- FLOATING: HorizontalFloatingToolbar + navigationBarsPadding(), floats above content

## Theme Extension

Add to Theme data class:
- topBarMode: TopBarMode = TopBarMode.PINNED
- bottomBarMode: BottomBarMode = BottomBarMode.STANDARD

## Persistence

SettingsPrefsImpl: add `top_bar_mode` and `bottom_bar_mode` string keys.
Read via valueOf(), write via .name.

## NLtimerScaffold Changes

### Top Bar Branch
- PINNED: keep CenterAlignedTopAppBar
- COLLAPSED: TopAppBar + exitUntilCollapsedScrollBehavior(), 32sp title, nestedScroll on Scaffold

### Bottom Bar Branch
- STANDARD: keep NavigationBar in Box bottom layer
- FLOATING: HorizontalFloatingToolbar as Box sibling, floating above content, 80.dp bottom padding

### Secondary Pages
Not affected, always pinned top bar + no bottom bar.

## Settings UI

In ThemeSettingsScreen, after "show borders", before ExpressivenessSection:
1. Top bar mode: SingleChoiceSegmentedButtonRow (PINNED/COLLAPSED)
2. Bottom bar mode: SingleChoiceSegmentedButtonRow (STANDARD/FLOATING)

## Files

| File | Change |
|------|--------|
| TopBarMode.kt (new) | enum |
| BottomBarMode.kt (new) | enum |
| EnumExt.kt | toDisplayString() for both |
| ThemeConfig.kt | 2 new fields |
| SettingsPrefsImpl.kt | 2 new keys + read/write |
| NLtimerScaffold.kt | branches + nestedScroll |
| AppTopAppBar.kt | collapsed mode composable |
| AppBottomNavigation.kt | floating mode composable |
| ThemeSettingsViewModel.kt | 2 mutation methods |
| ThemeSettingsScreen.kt | 2 selector UIs |
| ThemeSettingsRoute | pass 2 new callbacks |

## Display Strings

- TopBarMode.PINNED -> fixed
- TopBarMode.COLLAPSED -> collapsed
- BottomBarMode.STANDARD -> standard
- BottomBarMode.FLOATING -> floating
