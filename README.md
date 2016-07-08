ToastCompat
-----------

[![Build Status](https://travis-ci.org/kamikat/toast-compat.svg?branch=master)](https://travis-ci.org/kamikat/toast-compat)
[![JitPack.io](https://jitpack.io/v/moe.banana/toast-compat.svg)](https://jitpack.io/#moe.banana/toast-compat)

Protect toasts from being disabled by [feature](https://code.google.com/p/android/issues/detail?id=35013) on Android &gt;= 4.2 (Jelly Bean).

Usage
-----

Add dependency to gradle build script:

```gradle
repositories {
    maven { url "https://jitpack.io"  }
}

dependencies {
    compile 'moe.banana:toast-compat:<version>'
}
```

And toast:

```java
ToastCompat.makeText(context, "hello world!", Toast.LENGTH_SHORT).show();
```

License
-------

(The MIT License)
