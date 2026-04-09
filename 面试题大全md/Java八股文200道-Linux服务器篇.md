# Java八股文200道

## 四、Linux服务器篇

### 81、**什么是Linux？**

```
Linux是一个开源的类Unix操作系统内核，广泛用于服务器、台式机和嵌入式系统。它以稳定性、安全性和灵活性而闻名。
```

### 82、Linux常用命令

```
### 1、目录操作
cd：切换目录
pwd：显示当前所在的位置
mkdir：创建目录
ls：查看目录下包含的子目录和文件，ls  -l   等价ll
rmdir：删除目录
### 2、文件操作
touch：创建文件
rm：删除文件或目录  -rf   
find：查找文件
cp：拷贝文件或目录
mv：移动文件或目录，也可以用于重命名
### 3、文件编辑和查看内容
vi：编辑文件
vim：编辑文件（vi升级版）
cat   more  less   head   tail：查看文件内容
### 4、网络和防火墙
查看网络：
ip addr
ifconfig
防火墙：
systemctl    start   firewalld
systemctl    stop   firewalld
systemctl    status   firewalld
### 5、压缩和解压缩
打包或压缩：c
tar     -zcvf    压缩包名    要压缩的文件或目录名
解压缩：x
tar      -zxvf   压缩包名   
### 6、进程操作
ps  -ef |  grep   筛选条件
top
kill   -9   进程编号
### 7、查看磁盘和内存
dh  -f  查看磁盘分区情况
free    查看内存使用情况
### 8、切换用户
su   用户名
### 9、以管理员身份运行
sudo   命令
### 10、文件（目录）权限
修改权限命令chmod：chmod   777   1.txt
### 11、重启和关闭
重启
**reboot**
关闭
shutdown  -h  now
### 12、服务管理
开启服务：systemctl    start  服务名
关闭服务：systemctl    stop   服务名
查看服务状态：systemctl    status  服务名
设置开机启动服务：systemctl   enable   服务名
```



### 83、linux如何修改网络地址？

```
vi   /etc/sysconfig/network-scripts/ifcfg-eth0  ，其中eth0为网卡名称

DEVICE=eth0
BOOTPROTO=static 
ONBOOT=yes

IPADDR=192.168.1.110
NETMASK=255.255.255.0
GATEWAY=192.168.1.1
DNS1=114.114.114.114
DNS2=8.8.8.8   (可选的)
```

### 84、*查看某端口是否被占用?*

```
方法一：使用 netstat 命令
netstat 是一个网络统计工具，可以显示网络连接、路由表、接口统计等
sudo netstat -tuln | grep :端口号

方法二：使用 ss 命令
ss 是 netstat 的替代品，功能更强大且速度更快。
sudo ss -tuln | grep :端口号

方法三：使用 lsof 命令
lsof 可以列出当前系统打开的文件和使用这些文件的进程。
sudo lsof -i :端口号
```



### 85、linux**如何查看系统日志？**

```
在Linux系统中，我们可以使用以下几种方法来查看系统日志：

1、使用命令行工具查看系统日志：
使用tail命令查看最新的系统日志：tail -f /var/log/syslog
使用less命令浏览和搜索系统日志：less /var/log/syslog
使用grep命令过滤和搜索系统日志：grep "error" /var/log/syslog

2、使用journalctl命令查看系统日志：
journalctl命令可以显示系统日志的详细信息，默认按时间顺序列出。
通过添加选项，如-f（实时更新）、-u（按单元，如服务单位）等，可以实现不同的查看方式。例如：
journalctl -f：实时更新的系统日志
journalctl -u sshd：仅显示与sshd服务有关的日志

3、使用GUI工具查看系统日志：
Linux系统提供了一些图形界面的日志查看工具，如gnome-logs、KSystemLog等。
这些工具通常以图形化的方式显示系统日志，可以更方便地浏览和搜索。
```



### 86、**什么是Nginx？**

```
Nginx是一个开源的高性能HTTP服务器和反向代理服务器，也可以用于处理HTTPS、SMTP、POP3和IMAP协议。它以高性能、稳定性、丰富的功能、简单的配置和低资源消耗而闻名。
```



### 87、**请解释Nginx服务器上的Master和Worker进程分别是什么？**

```
Master进程：负责读取和验证配置文件、管理worker进程。它启动后，通过一个for循环来接收和处理外部信号。
Worker进程：负责处理实际的客户端请求。它由master进程通过fork()函数产生，每个子进程执行一个for循环来实现Nginx服务器对事件的接收和处理。
```



### 88、**如何优化Nginx的性能？**

```
调整worker_processes和worker_connections来充分利用系统资源。
开启gzip压缩来减少网络传输数据量。
使用缓存静态文件，减少对后端服务器的请求。
使用keepalive连接来减少TCP握手的开销。
优化SSL配置，如启用SSL session cache。
使用负载均衡和HTTP/2来提升并发处理能力。
```



### 89、**正向代理和反向代理的区别。**

```
- 正向代理：代理服务器作用在客户端，客户端通过代理服务器发送请求到互联网上的服务器。客户端需要设置代理服务器的IP地址和端口。
- 反向代理：代理服务器作用在服务器端，接收客户端的请求，然后将请求分发给具体的服务器进行处理，再将服务器的响应结果反馈给客户端。对于客户端而言，代理服务器就像是原始服务器，无需进行特别设置。
```



### 90、Nginx负载均衡的策略

```
（1）轮询
（2）权重
（3）ip_hash
（4）最小连接数least_conn
（5）最小响应时间fair
（6）url_hash

```

