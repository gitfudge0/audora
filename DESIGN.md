# Design

Visual system for a native Android app (Jetpack Compose + Material 3). Color
is authored in OKLCH as the source of truth; the sRGB hex column is the
Compose bridge (`Color(0xFF……)`), to be verified against a real OKLCH→sRGB
conversion in code, not eyeballed.

## Theme

Dark. Scene sentence: a music collector at home in the evening, lamp-lit room,
settled into a long focused library-cleanup session, scanning dense track
lists and judging album-art quality side by side. The room is dim and cool;
the tool should feel like warm-glowing audio equipment in it, not a bright
screen shouting back.

Color strategy: **Restrained**. Cool near-black tinted neutrals carry the
surface; a single warm amber/copper accent (the glow of analog gear) carries
primary action, selection, and "active". This deliberately rejects the
category reflexes: not streaming-green, not Apple-red, not neon-on-black.

## Color

OKLCH source of truth, sRGB bridge for Compose. Neutrals are tinted toward a
cool hue (250); never pure `#000`/`#fff`.

| Role | OKLCH | sRGB (verify) |
|---|---|---|
| `background` | `oklch(0.16 0.012 250)` | `#14161A` |
| `surface` | `oklch(0.20 0.012 250)` | `#1B1E23` |
| `surfaceVariant` (rows) | `oklch(0.235 0.012 250)` | `#22252B` |
| `surfaceElevated` (sheets, bars) | `oklch(0.27 0.013 250)` | `#292D34` |
| `outline` | `oklch(0.34 0.012 250)` | `#3A3E47` |
| `outlineSubtle` (row dividers) | `oklch(0.27 0.010 250)` | `#282B31` |
| `onSurface` (primary text) | `oklch(0.95 0.008 250)` | `#ECEEF2` |
| `onSurfaceVariant` (secondary) | `oklch(0.73 0.010 250)` | `#A8ADB6` |
| `onSurfaceFaint` (tertiary) | `oklch(0.58 0.010 250)` | `#7E838C` |
| `accent` (primary/selected) | `oklch(0.78 0.125 65)` | `#E0A35B` |
| `accentContainer` | `oklch(0.42 0.075 62)` | `#6F4D29` |
| `onAccent` | `oklch(0.22 0.030 65)` | `#2A2117` |
| `status.ok` | `oklch(0.74 0.095 156)` | `#76B793` |
| `status.warn` (low-quality) | `oklch(0.82 0.130 92)` | `#E0BC60` |
| `status.missing` (error) | `oklch(0.68 0.150 26)` | `#D86B57` |

Accent appears on roughly <= 10% of any screen: primary buttons, current
selection, active filter, focused field. Status colors are paired with an
icon and a text label, never color alone (see PRODUCT.md accessibility).

## Typography

One family: **Inter** (bundled), variable. No display face; product UI labels
in a display font is a ban. Tabular figures for track/disc numbers, durations,
bitrate, file size.

Scale ratio ~1.2 (tight, product-appropriate). Hierarchy via weight contrast,
not size alone.

| Token | Size / Line | Weight | Use |
|---|---|---|---|
| `titleScreen` | 22 / 28 | 600 | Screen headers |
| `titleSection` | 17 / 24 | 600 | Album/group headers |
| `bodyStrong` | 15 / 20 | 550 | Track title |
| `body` | 15 / 20 | 400 | Field values |
| `label` | 13 / 18 | 500 | Field labels, chips |
| `meta` | 12 / 16 | 450 | Artist · year · format line, counts |
| `mono` | 13 / 18 | 450 | Paths, raw tag dumps (Inter tabular) |

Body/prose measure caps at ~70ch (lyrics preview, descriptions). Dense
tabular UI may run wider.

## Surfaces & Elevation

Near-black means shadows barely read. Elevate with **tonal layers**, not drop
shadows: `background` → `surface` → `surfaceVariant` → `surfaceElevated`.
Bottom sheets and the batch action bar sit on `surfaceElevated` with a 1px
`outline` hairline rather than a heavy shadow. No glassmorphism.

## Components

Every interactive component ships all states: default, hover/pressed,
focus-visible, selected, disabled, loading, error.

- **Track row**: leading album-art tile (56dp, rounded 8dp, placeholder with
  a faint note glyph when missing), title (`bodyStrong`), meta line
  (artist · year · format · bitrate). Trailing status cluster: three small
  chips (Art / Lyrics / Tags) each = icon + state color + terse label.
  Selected state = `accentContainer` fill + accent leading bar is **banned**;
  use a full `accentContainer` row tint + checkbox instead.
- **Art tile / art picker**: art is hero. Quality detection shows pixel
  dimensions and a warn/ok/missing chip. Replacement is a comparison view:
  current vs candidate, both at real size, source labeled.
- **Metadata editor**: grouped fields (Core / Credits / Album / Advanced),
  per-field "differs across selection" indicator in batch mode, diff
  highlight on changed fields, explicit Save (writes file) vs Revert.
- **Batch action bar**: appears on selection on `surfaceElevated`, shows
  count, exposes Fix Art / Get Lyrics / Edit Tags as the batch verbs.
- **Status chip**: `ok` / `warn` / `missing`, always icon + label + color.
- **Skeletons** for the library scan, not centered spinners. Empty states
  teach the next action (pick a folder, scan, fix).

## Motion

150–250ms, ease-out (quart/expo), state-only: selection, sheet in/out, chip
state change, scan progress, save confirmation. No page-load choreography, no
bounce/elastic, no animation of layout properties. Respect
`Settings.Global.ANIMATOR_DURATION_SCALE` / reduced-motion: cut to a fast
fade.

## Iconography

One set: **Material Symbols (Rounded)**. Do not mix icon families.

## Layout

Standard Android structure: top app bar, optional bottom sheet for edit/pick,
selection-driven contextual action bar. Predictable list grid; consistency is
the affordance. Spacing varies for rhythm (group headers breathe, rows are
dense); not uniform padding everywhere. Not everything gets a card; the
library is a list, not a card grid.
