# API of Android Network Diagnosis

## API概览
接口       | 说明
------------|-------------------------------------
Ping        | ping需要检测的域名
TcpPing     | 测试TCP端口的延迟情况
RtmpPing    | ping rtmp域名
TraceRoute  | 利用ICMP协议定位您的计算机和目标计算机之间的所有路由器





### Ping

接口调用：

```
Ping.start("www.baidu.com", 10, new TestLogger(), new Ping.Callback() {
            @Override
            public void complete(Ping.Result r) {
                ...
            }
        });
```

单元测试输出：

```
Started running tests
I/System.out: [CDS][DNS] getAllByNameImpl netId = 0
D/libc-netbsd: [getaddrinfo]: mtk hostname=www.baidu.com; servname=(null); cache_mode=(null), netid=0; mark=0
D/libc-netbsd: getaddrinfo: www.baidu.com get result from proxy >>
I/System.out: propertyValue:true
I/System.out: PING 182.61.200.6 (182.61.200.6) 56(84) bytes of data.
    64 bytes from 182.61.200.6: icmp_seq=1 ttl=52 time=24.7 ms
I/System.out: 64 bytes from 182.61.200.6: icmp_seq=2 ttl=52 time=33.1 ms
I/System.out: 64 bytes from 182.61.200.6: icmp_seq=3 ttl=52 time=26.9 ms
I/System.out: 64 bytes from 182.61.200.6: icmp_seq=4 ttl=52 time=27.1 ms
I/System.out: 64 bytes from 182.61.200.6: icmp_seq=5 ttl=52 time=29.4 ms
I/System.out: 64 bytes from 182.61.200.6: icmp_seq=6 ttl=52 time=31.4 ms
I/System.out: 64 bytes from 182.61.200.6: icmp_seq=7 ttl=52 time=26.6 ms
I/System.out: 64 bytes from 182.61.200.6: icmp_seq=8 ttl=52 time=27.5 ms
I/System.out: 64 bytes from 182.61.200.6: icmp_seq=9 ttl=52 time=27.6 ms
I/System.out: 64 bytes from 182.61.200.6: icmp_seq=10 ttl=52 time=24.9 ms
    --- 182.61.200.6 ping statistics ---
    10 packets transmitted, 10 received, 0% packet loss, time 1839ms
    rtt min/avg/max/mdev = 24.781/27.981/33.186/2.545 ms
I/TestRunner: finished: testOK(com.qiniu.android.netdiag.PingTest)
    passed: testOK(com.qiniu.android.netdiag.PingTest)
Disconnected from the target VM, address: 'localhost:8601', transport: 'socket'

Tests ran to completion.
```


### TcpPing
接口调用：

```
TcpPing.start("www.baidu.com", new TestLogger(), new TcpPing.Callback() {
            @Override
            public void complete(TcpPing.Result r) {
                ...
            }
        });
```

单元测试输出：

```
Started running tests
I/System.out: debugger has settled (1459)
I/TestRunner: started: testOK(com.qiniu.android.netdiag.TcpPingTest)
I/System.out: [CDS][DNS] getAllByNameImpl netId = 0
D/libc-netbsd: getaddrinfo: www.baidu.com get result from proxy >>
I/System.out: connect to 182.61.200.6:80
I/System.out: [socket][0] connection /182.61.200.6:80;LocalPort=51264(20000)
    [CDS]connect[/182.61.200.6:80] tm:20
D/Posix: [Posix_connect Debug]Process com.qiniu.android.netdiag.test :80 
I/System.out: [socket][/192.168.31.232:51264] connected
    [CDS]close[51264]
I/System.out: [socket][1] connection /182.61.200.6:80;LocalPort=48819(20000)
I/System.out: [CDS]connect[/182.61.200.6:80] tm:20
D/Posix: [Posix_connect Debug]Process com.qiniu.android.netdiag.test :80 
I/System.out: [socket][/192.168.31.232:48819] connected
    [CDS]close[48819]
I/System.out: [socket][2] connection /182.61.200.6:80;LocalPort=37218(20000)
    [CDS]connect[/182.61.200.6:80] tm:20
D/Posix: [Posix_connect Debug]Process com.qiniu.android.netdiag.test :80 
I/System.out: [socket][/192.168.31.232:37218] connected
I/System.out: [CDS]close[37218]
I/TestRunner: finished: testOK(com.qiniu.android.netdiag.TcpPingTest)
    passed: testOK(com.qiniu.android.netdiag.TcpPingTest)

Tests ran to completion.
```

### RtmpPing
接口调用：

```
RtmpPing.start("pili-live-rtmp.pilitest.qiniucdn.com", new TestLogger(), new RtmpPing.Callback() {
            @Override
            public void complete(RtmpPing.Result r) {
                result = r;
                c.countDown();
            }
```

单元测试输出：

```
Started running tests
D/libc-netbsd: getaddrinfo: pili-live-rtmp.pilitest.qiniucdn.com get result from proxy >>
I/System.out: propertyValue:true
I/System.out: [CDS][DNS] getAllByNameImpl netId = 0
I/System.out: connect to 60.195.240.74:1935
    [CDS]rx timeout:30000
    [socket][0] connection /60.195.240.74:1935;LocalPort=47495(20000)
I/System.out: [CDS]connect[/60.195.240.74:1935] tm:20
D/Posix: [Posix_connect Debug]Process com.qiniu.android.netdiag.test :1935 
I/System.out: [socket][/192.168.31.232:47495] connected
    [socket][1] connection /60.195.240.74:1935;LocalPort=49283(20000)
    [CDS]connect[/60.195.240.74:1935] tm:20
D/Posix: [Posix_connect Debug]Process com.qiniu.android.netdiag.test :1935 
I/System.out: [socket][/192.168.31.232:49283] connected
I/System.out: [CDS]close[49283]
    1: conn:60 handshake:92
I/TestRunner: finished: testOK(com.qiniu.android.netdiag.RtmpPingTest)
    passed: testOK(com.qiniu.android.netdiag.RtmpPingTest)

Tests ran to completion.
```


### TraceRoute  
接口调用：

```
Task t = TraceRoute.start("www.baidu.com", new Output() {
            @Override
            public void write(String line) {
                System.out.println("test> " + line);
            }
        }, new TraceRoute.Callback() {
            @Override
            public void complete(TraceRoute.Result r) {
                System.out.println(r.content());
                l.add(r);
                c.countDown();
            }
        });
```

单元测试输出：

```
Started running tests
D/libc-netbsd: getaddrinfo: www.baidu.com get result from proxy >>
I/System.out: propertyValue:true
I/System.out: test> 1.	192.168.31.1		9ms	
I/System.out: test> 2.	124.15.48.1		7ms	
I/System.out: test> 3.		 * 	
I/System.out: test> 4.		 * 	
I/System.out: test> 5.		 * 	
I/System.out: test> 6.		 * 	
I/System.out: test> 7.		 * 	
I/System.out: test> 8.		 * 	
I/System.out: test> 9.		 * 	
I/System.out: test> 10.		 * 	
I/System.out: test> 11.		 * 	
I/System.out: test> 12.		 * 	
I/System.out: test> 13.		182.61.200.6		47.6 ms	
    1.	192.168.31.1		9ms	2.	124.15.48.1		7ms	3.		 * 	4.		 * 	5.		 * 	6.		 * 	7.		 * 	8.		 * 	9.		 * 	10.		 * 	11.		 * 	12.		 * 	13.		182.61.200.6		47.6 ms	
I/TestRunner: finished: testTrace(com.qiniu.android.netdiag.TraceRouteTest)
    passed: testTrace(com.qiniu.android.netdiag.TraceRouteTest)

Tests ran to completion.
```

