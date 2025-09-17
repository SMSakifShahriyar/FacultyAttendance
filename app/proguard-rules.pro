# Add project specific ProGuard rules here.

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory$ViewModelFactoriesEntryPoint

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Location Services
-keep class com.google.android.gms.location.** { *; }

# Keep data classes
-keep class com.sakif.facultyattendance.data.** { *; }
