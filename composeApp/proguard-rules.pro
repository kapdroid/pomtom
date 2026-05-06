# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.kapdroid.pomtom.**$$serializer { *; }
-keepclassmembers class com.kapdroid.pomtom.** {
    *** Companion;
}
-keepclasseswithmembers class com.kapdroid.pomtom.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Compose / Lifecycle / Navigation handle their own rules via consumer-proguard.

# Koin
-keep class org.koin.** { *; }
-keepclassmembers class * {
    @org.koin.core.annotation.* *;
}

# Napier
-dontwarn io.github.aakira.napier.**

# Coil
-dontwarn coil3.**

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel { *; }