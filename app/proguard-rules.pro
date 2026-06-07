# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# jaudiotagger instantiates its audio/tag/frame-body classes reflectively by
# name (e.g. FrameBodyTIT2). Under R8 these get renamed and reflection fails
# with "Class<...> cannot be instantiated", silently breaking tag writes in
# release builds. Keep the whole library and its reflected members.
-keep class org.jaudiotagger.** { *; }
-dontwarn org.jaudiotagger.**
