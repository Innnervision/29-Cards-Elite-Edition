# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve debugging line numbers and source files for Play Console stacktraces
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep R8 / ProGuard mapping metadata for Play Console
-renamesourcefileattribute SourceFile

# Room Database
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public void clearAllTables();
    <methods>;
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Game Models, State, and Engine Serialization
-keep class com.example.game.model.** { *; }
-keep class com.example.game.data.** { *; }
-keep class com.example.game.engine.** { *; }
-keep class com.example.game.economy.** { *; }

# Moshi & Retrofit
-keepclassmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
}
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**
-dontwarn retrofit2.**

# Google Mobile Ads
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep public class com.google.ads.** {
   public *;
}
-dontwarn com.google.android.gms.ads.**

# Coroutines
-dontwarn kotlinx.coroutines.**
