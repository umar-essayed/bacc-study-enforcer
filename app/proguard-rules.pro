# Apple-inspired minimalism app proguard rules
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* *;
}
