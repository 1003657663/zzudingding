# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\�����װλ��\andsdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
#-keepattributes *Annotation*

-dontobfuscate

-dontwarn org.apache.harmony.awt.datatransfer.* 
-dontwarn com.sun.mail.imap.protocol.* 
-dontwarn org.apache.harmony.awt.ContextStorage 
-dontwarn javax.activation.CommandInfo

#-keep interface ** {*;}
#-keep class com.google.gson.** {*;}
#-keep interface android.support.v4.app.** { *;}
#-keep class android.support.v4.** { *; }
#-keep public class * extends android.support.v4.**
#-keep public class * extends android.app.Fragment
#-keep interface android.support.v7.app.** {*;}
#-keep class android.support.v7.** {*;}
#-keep public class * extends android.support.v7.**
#-keep class com.sun.mail.**{*;}
#-keep class javax.mail.**{*;}
#-keep class com.android.volley.**{*;}
#-keep class org.jsoup.** {*;}
#-keep class io.yunba.android.** {*;}
#-keep class org.eclipse.paho.client.mqttv3.** {*;}
#-keepclasseswithmembernames class com.codeevery.InfoShow.SpendMoneyBean {*;}
#-keepclasseswithmembernames class com.codeevery.InfoShow.RootBean {*;}
#-keepclasseswithmembernames class com.codeevery.InfoShow.SpendMoney** {*;}
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
#-dontwarn org.apache.harmony.awt.datatransfer.*
#-dontwarn com.sun.mail.imap.protocol.*
#-dontwarn org.apache.harmony.awt.ContextStorage
#-dontwarn javax.activation.CommandInfo
#-dontwarn android.support.v4.**
#-dontwarn **CompatHoneycomb
#-dontwarn **CompatHoneycombMR2
#-dontwarn **CompatCreatorHoneycombMR2
