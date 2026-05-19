# Product

## Register

product

## Users

Music collectors and audiophiles who keep a local library on their Android
device: ripped CDs, Bandcamp/Qobuz/HDtracks downloads, and years of
accumulated files from mixed sources. They are detail-oriented and
tech-comfortable. They already know desktop tools like Mp3tag and MusicBrainz
Picard, but the messy library lives on the phone and they want to fix it
there.

Their context: at home, in a focused curation session, often a long one. The
job to be done is turning a folder of inconsistent, half-tagged, art-missing
music into a clean library that displays correctly in any player (the system
player, Poweramp, the car head unit, the lock screen). Success looks like: a
track that had no art, a wrong artist, and no lyrics now shows correctly
everywhere, an entire album cleaned in a couple of taps, and never a corrupted
or lost file.

## Product Purpose

A local-music workspace for three jobs: (1) find and download synced `.lrc`
lyrics next to each track, (2) edit metadata so libraries read cleanly in any
player, (3) detect missing or low-quality album art and replace it with
high-resolution covers. It exists because phone music players only consume
metadata, they never help you fix it, and the desktop tools that do are not on
the phone. Success is a measurable drop in "unknown artist", missing-art, and
missing-lyrics tracks, with zero file corruption.

## Brand Personality

Precise, trustworthy, unobtrusive. The voice of a good piece of audio
equipment: it tells you the truth about what is there, never overstates, never
damages your files, and gets out of the way. The emotional goal is calm
control: the mess is becoming order and the library is in safe hands. Not
playful, not loud, not "magical".

## Anti-references

- Streaming-app gloss (Spotify, Apple Music). This is not for casual
  listening; do not borrow that visual language.
- Toolbar-soup freeware tag editors (TagScanner, Mp3tag at its densest):
  1990s density, every function a tiny button, no hierarchy.
- The "AI music app" neon-gradient-on-black cliche.
- One-tap "magically fix everything" with no preview and no undo. Destructive
  confidence is the opposite of this product.

## Design Principles

1. **Never lie about state.** Show exactly what each track has, is missing, or
   has at low quality. Honesty over optimism; the status display is the
   product's spine.
2. **Preview before commit.** Every tag, art, or lyric change is reviewable
   and reversible. Never silently overwrite the user's files; writes are
   deliberate and survivable.
3. **The art is the content.** Album art is the strongest signal of library
   health. Treat it as hero imagery, not a thumbnail.
4. **Batch is a first-class verb.** Cleanup is repetitive. Anything you can do
   to one track you can do to a selection or a whole album.
5. **Disappear into the task.** This is a workspace, not a destination. Speed,
   legibility, and trust beat decoration every time.

## Accessibility & Inclusion

WCAG AA. Dark theme must hold AA contrast (>= 4.5:1 body, >= 3:1 large/UI) on
near-black surfaces. Touch targets >= 48dp. Respect reduced-motion. Color is
never the only carrier of meaning: art-quality and lyric/tag status always use
an icon plus text label in addition to color, since the core status vocabulary
must survive color-blindness and glare.
