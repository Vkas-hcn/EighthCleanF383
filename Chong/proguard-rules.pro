-keepattributes !SourceFile
-dontshrink

-keep class a.A{*;}
-keep class b.B{*;}
-keep class c.C{*;}
-keep class d.D{*;}


#appsflyer start
# keep init adpost
-keep class com.appsflyer.** { *; }
-keep class kotlin.jvm.internal.** { *; }
-keep public class com.android.installreferrer.** { *; }
#appsflyer end


-keep class com.bytedance.sdk.** { *; }

#加固
-keep class kotlin.**{*;}
-keep class kotlinx.**{*;}
-keep class androidx.**{*;}
-keep class android.**{*;}
-keep class com.facebook.**{*;}
-keep class com.tencent.mmkv.**{*;}
-keep class okhttp3.** {*;}
-keep class com.google.**{*;}
-keep class com.android.**{*;}


#topon 混淆
# Vungle
-dontwarn com.vungle.ads.**
-keepclassmembers class com.vungle.ads.** {
  *;
}
-keep class com.vungle.ads.**



# Google
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**




# START OkHttp + Okio
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**


# A resource is loaded with a relative path so the package of this class must be preserved.
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz


# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*


# OkHttp platform used only on JVM and when Conscrypt and other security providers are available.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**


# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*


# END OkHttp + Okio


# START Protobuf
-dontwarn com.google.protobuf.**
-keepclassmembers class com.google.protobuf.** {
 *;
}
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }


# END Protobuf
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.mbridge.** {*; }
-keep interface com.mbridge.** {*; }
-dontwarn com.mbridge.**
-keepclassmembers class **.R$* { public static final int mbridge*; }

-keep public class com.mbridge.* extends androidx.** { *; }
-keep public class androidx.viewpager.widget.PagerAdapter{*;}
-keep public class androidx.viewpager.widget.ViewPager.OnPageChangeListener{*;}
-keep interface androidx.annotation.IntDef{*;}
-keep interface androidx.annotation.Nullable{*;}
-keep interface androidx.annotation.CheckResult{*;}
-keep interface androidx.annotation.NonNull{*;}
-keep public class androidx.fragment.app.Fragment{*;}
-keep public class androidx.core.content.FileProvider{*;}
-keep public class androidx.core.app.NotificationCompat{*;}
-keep public class androidx.appcompat.widget.AppCompatImageView {*;}
-keep public class androidx.recyclerview.*{*;}
-keep class com.mbridge.msdk.foundation.tools.FastKV{*;}
-keep class com.mbridge.msdk.foundation.tools.FastKV$Builder{*;}





