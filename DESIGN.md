# Design

Visual system for a native Android app (Jetpack Compose + Material 3). Twin
light/dark scheme generated from one OKLCH source of truth; the sRGB hex
column is the Compose bridge. Color is monochrome-rigorous: ink-on-cream in
light, bone-on-graphite in dark. Status colors carry the *only* chromatic
vocabulary; the brand never asserts itself with hue.

## Theme

Two modes, one system. Default follows OS preference; user can override in
Settings.

**Light** — a quiet workbench in daylight. Cool-tinted off-white surface,
ink-black accent, hairline borders. Calm, precise, paper-like.

**Dark** — the same workbench at night. Cool near-black surface, bone-white
accent, the same hairlines. Not a "dim mode" of light, not a streaming-app
black; an intentional second instrument with the same vocabulary.

**Color strategy: monochrome + status.** Primary action, current selection,
focus, and "active" are carried by a single neutral accent (ink in light,
bone in dark). The only chromatic colors in the entire UI are the three
status hues — `ok`, `warn`, `missing` — paired with an icon and label. This
deliberately rejects category reflexes (streaming green, banking red, neon
gradients) and reinforces the product's core principle: **color = status,
nothing else.**

## Color

OKLCH source of truth, sRGB bridge for Compose. Neutrals are tinted toward a
cool hue (250); never pure `#000` / `#fff`.

### Light

| Role | OKLCH | sRGB |
|---|---|---|
| `background` | `oklch(0.985 0.003 250)` | `#F7F8FA` |
| `surface` | `oklch(0.965 0.004 250)` | `#F1F2F5` |
| `surfaceVariant` (rows) | `oklch(0.935 0.005 250)` | `#E7E9ED` |
| `surfaceElevated` (sheets, cards) | `oklch(1.000 0 0)` | `#FFFFFF` |
| `outline` | `oklch(0.82 0.006 250)` | `#C8CBD2` |
| `outlineSubtle` (row dividers) | `oklch(0.91 0.005 250)` | `#DEE0E5` |
| `onSurface` (primary text) | `oklch(0.22 0.010 250)` | `#23262C` |
| `onSurfaceVariant` (secondary) | `oklch(0.45 0.010 250)` | `#5E626B` |
| `onSurfaceFaint` (tertiary) | `oklch(0.62 0.008 250)` | `#878B93` |
| `accent` (ink) | `oklch(0.18 0.012 250)` | `#1B1D22` |
| `accentContainer` (selected fill) | `oklch(0.92 0.006 250)` | `#E2E5EA` |
| `onAccent` | `oklch(0.98 0.003 250)` | `#F7F8FA` |
| `status.ok` | `oklch(0.52 0.115 156)` | `#3F8A66` |
| `status.warn` | `oklch(0.62 0.140 75)` | `#B07A1C` |
| `status.missing` | `oklch(0.54 0.160 26)` | `#B84A36` |

### Dark

| Role | OKLCH | sRGB |
|---|---|---|
| `background` | `oklch(0.16 0.010 250)` | `#14161A` |
| `surface` | `oklch(0.20 0.010 250)` | `#1B1E23` |
| `surfaceVariant` (rows) | `oklch(0.235 0.010 250)` | `#22252B` |
| `surfaceElevated` (sheets, cards) | `oklch(0.27 0.011 250)` | `#292D34` |
| `outline` | `oklch(0.36 0.010 250)` | `#3D414A` |
| `outlineSubtle` (row dividers) | `oklch(0.27 0.008 250)` | `#282B31` |
| `onSurface` (primary text) | `oklch(0.95 0.006 250)` | `#ECEEF2` |
| `onSurfaceVariant` (secondary) | `oklch(0.74 0.008 250)` | `#AAAFB7` |
| `onSurfaceFaint` (tertiary) | `oklch(0.58 0.008 250)` | `#7E838C` |
| `accent` (bone) | `oklch(0.96 0.005 250)` | `#EFF1F4` |
| `accentContainer` (selected fill) | `oklch(0.32 0.010 250)` | `#363A42` |
| `onAccent` | `oklch(0.18 0.010 250)` | `#1B1D22` |
| `status.ok` | `oklch(0.74 0.095 156)` | `#76B793` |
| `status.warn` | `oklch(0.82 0.130 92)` | `#E0BC60` |
| `status.missing` | `oklch(0.68 0.150 26)` | `#D86B57` |

Accent appears on ≤ 10% of any screen: primary buttons, current selection,
active filter, focused field. Status colors are always paired with an icon
and a text label, never color alone.

All foreground/background pairings hold WCAG AA: ≥ 4.5:1 body, ≥ 3:1 large /
UI elements, in both modes.

## Shape

Larger radii than stock Material. Cards breathe; chips and CTAs are full
pills. Hairlines, not shadows.

| Token | Radius | Use |
|---|---|---|
| `xs` | 6dp | Inline tags, format pills |
| `sm` | 10dp | Art tiles ≤ 56dp, dense chips |
| `md` | 14dp | Standard chips, text fields |
| `lg` | 20dp | Cards, panels, sheets header |
| `xl` | 28dp | Hero cards, art tiles ≥ 96dp |
| `pill` | 50% | Buttons, filter chips, segmented tabs |

## Typography

One family: **Inter** (variable, bundled — system sans is the offline
fallback). No display face. Tabular figures for any number that exists to be
read precisely (track number, duration, bitrate, file size, counts, %).

Scale ratio ~1.2. Hierarchy via weight contrast, not size alone.

| Token | Size / Line | Weight | Use |
|---|---|---|---|
| `numericHero` | 32 / 36 | 600 (tabular) | Library stats, big readouts |
| `numericLarge` | 22 / 28 | 550 (tabular) | Album/track counts, durations |
| `titleScreen` | 22 / 28 | 600 | Screen headers |
| `titleSection` | 17 / 24 | 600 | Album/group headers |
| `bodyStrong` | 15 / 20 | 550 | Track title, row primary |
| `body` | 15 / 20 | 400 | Field values |
| `label` | 13 / 18 | 500 | Field labels, chips |
| `meta` | 12 / 16 | 450 | Artist · year · format, captions |
| `mono` | 13 / 18 | 450 (tabular) | Paths, raw tag dumps |

Body/prose measure caps at ~70ch (lyrics preview, descriptions). Dense
tabular UI may run wider.

## Surfaces & Elevation

Tonal layers, not drop shadows: `background` → `surface` → `surfaceVariant`
→ `surfaceElevated`. Bottom sheets and the batch action bar sit on
`surfaceElevated` with a 1px `outline` hairline rather than a shadow. No
glassmorphism. No blur.

## Motion

150–250ms, ease-out (quart/expo), state-only: selection, sheet in/out, chip
state change, scan progress, save confirmation. No page-load choreography,
no bounce/elastic, no layout-property animation. Respect
`Settings.Global.ANIMATOR_DURATION_SCALE` / reduced-motion: cut to a fast
fade.

## Iconography

One set: **Material Symbols (Rounded)**. Do not mix icon families.

## Components

Every interactive component ships all states: default, hover/pressed,
focus-visible, selected, disabled, loading, error. Each lives in
`ui/components/` and consumes design tokens only — screen files never set
raw `Color(0x...)`.

### Building blocks

- **`AppTopBar`** — title + optional nav + actions, on `background`, no
  shadow. Title is `titleScreen`.
- **`PrimaryButton`** / **`SecondaryButton`** / **`GhostButton`** /
  **`DestructiveButton`** — pill-shape, 48dp min height. Primary is filled
  with `accent`; Secondary is `surfaceVariant` with `onSurface`; Ghost is
  text-only; Destructive uses `status.missing`.
- **`AppIconButton`** — square or pill, 40dp default. Pressed = tonal fill
  bump, no scale animation.
- **`Card`** — `surface` background, 1dp `outlineSubtle` border, `lg` radius.
- **`Panel`** — flat `surfaceVariant`, no border, `lg` radius. For grouped
  field clusters.
- **`Sheet`** — `surfaceElevated`, hairline top border, `xl` radius top
  corners only.
- **`StatusChip`** — icon + label + status color tint (16% alpha bg, full
  color fg). Generalized to take any (icon, label, color); convenience
  wrappers per status enum live alongside.
- **`FilterChip`** — pill, `surfaceVariant` default, `accent` filled when
  selected, optional leading icon.
- **`CountChip`** — pill with a number; `surfaceVariant` bg, `onSurface` fg.
- **`AppTextField`** — single-line or multi-line, `surfaceVariant` bg, no
  baseline; focus = 1dp `accent` border.
- **`ListRow`** — leading / headline / supporting / trailing slot row. Min
  height 64dp. Selected = `accentContainer` fill (no leading bar, ever).
- **`ArtTile`** — sizes `sm 40dp`, `md 56dp`, `lg 96dp`, `hero` (fills
  width). Rounded per shape token. Placeholder = note glyph on `surface`.
- **`SectionHeader`** — `titleSection` + optional trailing action; 20dp top,
  12dp bottom rhythm so groups breathe.
- **`MetricReadout`** / **`MetricGroup`** — label (`label`) over value
  (`numericHero`/`numericLarge`), optional caption. Group renders a row of
  readouts separated by hairline dividers.
- **`EmptyState`** — icon + title + body + primary action. Centered, ~340dp
  body cap.
- **`Skeleton`** — `SkeletonRow`, `SkeletonTile`, `SkeletonText` —
  `surfaceVariant` blocks; no shimmer animation (state-only motion rule).
- **`AppBottomSheet`** / **`AppDialog`** — shells with drag handle,
  title row, content slot, footer action row.
- **`PillTabs`** / **`UnderlineTabs`** — pill segmented (Albums | Tracks
  scope switch); underline for in-page sub-tabs.
- **`Hairline`** — 1dp `outlineSubtle` divider; `InsetHairline` for list
  rows.

### Domain components (built on building blocks)

- **`TrackListRow`** — `ListRow` with `ArtTile.sm`, title (`bodyStrong`),
  meta line (artist · album · year · format), trailing status cluster
  (art / tags / lyrics chips). Selected = full `accentContainer` row tint +
  trailing checkbox. Leading accent bar is banned.
- **`AlbumListRow`** — `ListRow` with `ArtTile.md`, album title, artist ·
  track count · year, status chip cluster.
- **`BatchActionBar`** — pinned bottom on selection, `surfaceElevated`,
  hairline top, count + clear + verbs.
- **`MetadataEditor`** — grouped fields in `Panel`s (Core / Credits /
  Album / Advanced), per-field "differs across selection" indicator in batch
  mode, diff highlight on changed fields, explicit Save vs Revert.
- **`ArtPicker`** — current vs candidate at real size, source labeled.

## Layout

Standard Android structure: top app bar, optional bottom sheet for
edit/pick, selection-driven contextual action bar. Predictable list grid;
consistency is the affordance. Spacing varies for rhythm (group headers
breathe, rows are dense); not uniform padding everywhere. Not everything
gets a card; the library is a list, not a card grid.

## Source of truth

This file is the spec. The Compose implementation lives in
`app/src/main/java/dev/gitfudge/musicworkbench/ui/theme/` (tokens) and
`ui/components/` (components). If a screen needs a color, a shape, a text
style, or a UI element not listed here, the spec gets updated first and the
implementation second.
