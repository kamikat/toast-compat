ToastCompat
-----------

[![Build Status](https://travis-ci.org/kamikat/toast-compat.svg?branch=master)](https://travis-ci.org/kamikat/toast-compat)
[![JitPack.io](https://jitpack.io/v/moe.banana/toast-compat.svg)](https://jitpack.io/#moe.banana/toast-compat)

Polyfills toast notification feature on Android &gt;= 4.2 (Jelly Bean)

Usage
-----

Install from JitPack repository:

```groovy
repositories {
    ...
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
