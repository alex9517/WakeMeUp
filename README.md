# WakeMeUp (a simple timer)
This is an Android application targeting API 29 (Android 10).
See [About Android versions](#versions).

### WakeMeUp is not a typical alarm clock
I don't like alarm clocks because they irritate you with some unpleasant sound
until you switch it off.

So, I created this app that plays a short melody or some kind of beep sound
at the specified time, and that's all. You can react, or you can just say to
yourself: "OK, I've heard that", and continue to sleep. No pressure.

It's also possible to set several signals. But! Due to system policy of latest
Android OS versions, the minimum interval between signals can vary from 1 minute
to 15 minutes (low battery)! That is, you can set whatever you want, but there
is a high probability that your second (third, etc) signal will be moved to a
later time to comply with the system policy. So, 15 minutes interval must be
safe unless..

### Power Savers
If this app does not trigger signals at the right time, the most probable cause
is the interference of some OEM or 3rd party application that is supposed to
save the battery power. These apps usually kill anything that tries to start
during the doze mode. Unfortunately, the standard Android permissions cannot
solve this problem. That is, the following settings in `AndroidManifest.xml`:
```
   ...
   <uses-permission android:name="android.permission.WAKE_LOCK" />
   <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
   <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
   <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
   ...
```
may be necessary but not sufficient. Usually, to solve the problem, you have
to "whitelist" your app (whatever it means in the context of a specific mobile
device).

For example, in **Xiaomi Redmi Note 9** smartphone (Android 10) this app
(WakeMeUp) does not work normally unless you set **MIUI Battery saver** to
"No restrictions" [for this particular app]. One way to perform this: do a long
touch on the WakeMeUp's launcher icon, then "AppInfo", then look for "Battery
saver ..".


### <a name="versions">About Android versions</a>
This is an Android application targeting API 29 (Android 10),
and probably capable of working on newer versions. However, since some parts
of Android OS regularly undergo radical changes, I cannot gaurantee that this
specific version of **WakeMeUp** is going to work in future versions of Android.
The major problem of this app is that it uses Android's `AlarmManager` to play
the signals when the system is sleeping. But the system policy in general is to
save the energy, i.e., to stop the execution of nearly anything when user does
not interact with device.

The code of this app can be modified to run on older Android versions (it was
initially created for Android 6 / API 23). But it would take some efforts.
