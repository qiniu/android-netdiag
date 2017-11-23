# Network Diagnosis for Android

[![@qiniu on weibo](http://img.shields.io/badge/weibo-%40qiniutek-blue.svg)](http://weibo.com/qiniutek)
[![Software License](https://img.shields.io/badge/license-MIT-brightgreen.svg)](LICENSE.md)
[![Build Status](https://travis-ci.org/qiniu/android-netdiag.svg?branch=master)](https://travis-ci.org/qiniu/android-netdiag)
[![Latest Stable Version](http://img.shields.io/maven-central/v/com.qiniu/android-netdiag.svg)](https://github.com/qiniu/android-netdiag/releases)

## 用途

网络诊断库，支持Ping/TcpPing/Rtmp/TraceRoute/DNS/外部IP/外部DNS。

## 安装

通过Gradle

```groovy
compile 'com.qiniu:android-netdiag:0.1.1'
```

## 使用方法
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

### 所有单元测试

``` bash
./gradlew connectedAndroidTest

```


## 常见问题

## 代码贡献

详情参考[代码提交指南](https://github.com/qiniu/android-netdiag/blob/master/CONTRIBUTING.md)。

## 贡献记录

- [所有贡献者](https://github.com/qiniu/android-netdiag/contributors)

## 联系我们

- 如果有什么问题，可以到问答社区提问，[问答社区](http://qiniu.segmentfault.com/)
- 如果发现了bug， 欢迎提交 [issue](https://github.com/qiniu/android-netdiag/issues)
- 如果有功能需求，欢迎提交 [issue](https://github.com/qiniu/android-netdiag/issues)
- 如果要提交代码，欢迎提交 pull request
- 欢迎关注我们的[微信](http://www.qiniu.com/#weixin) [微博](http://weibo.com/qiniutek)，及时获取动态信息。

## 代码许可

The MIT License (MIT).详情见 [License文件](https://github.com/qiniu/android-netdiag/blob/master/LICENSE).
