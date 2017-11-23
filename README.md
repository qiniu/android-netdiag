# Network Diagnosis for Android

[![@qiniu on weibo](http://img.shields.io/badge/weibo-%40qiniutek-blue.svg)](http://weibo.com/qiniutek)
[![Software License](https://img.shields.io/badge/license-MIT-brightgreen.svg)](LICENSE.md)
[![Build Status](https://travis-ci.org/qiniu/android-netdiag.svg?branch=master)](https://travis-ci.org/qiniu/android-netdiag)
[![Latest Stable Version](http://img.shields.io/maven-central/v/com.qiniu/android-netdiag.svg)](https://github.com/qiniu/android-netdiag/releases)

## [中文](https://github.com/qiniu/android-netdiag/blob/master/README_cn.md)

## Summary

Network Diagnosis Library，support Ping/TcpPing/Rtmp/TraceRoute/DNS/external IP/external DNS。

## Install

gradle

```groovy
compile 'com.qiniu:android-netdiag:0.1.1'
```

## Usage
### Ping
```java
Ping.start("www.baidu.com", 10, new TestLogger(), new Ping.Callback() {
            @Override
            public void complete(Ping.Result r) {
                ...
            }
        });
```

### TcpPing
```java
TcpPing.start("www.baidu.com", new TestLogger(), new TcpPing.Callback() {
            @Override
            public void complete(TcpPing.Result r) {
                ...
            }
        });
```

### All Unit Test

``` bash
./gradlew connectedAndroidTest

```

## Faq


## Contributing

Please Look at[Contributing Guide](https://github.com/qiniu/android-netdiag/blob/master/CONTRIBUTING.md)。

## Contributors

- [Contributors](https://github.com/qiniu/android-netdiag/contributors)

## Contact us

- If you find any bug， please submit [issue](https://github.com/qiniu/android-netdiag/issues)
- If you need any feature， please submit [issue](https://github.com/qiniu/android-netdiag/issues)
- If you want to contribute, please submit pull request

## License

The MIT License (MIT). [License](https://github.com/qiniu/android-netdiag/blob/master/LICENSE).
