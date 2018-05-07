# fileserver
一个基于netty的文件服务器，可以用于分发应用程序，文件md5校验比对差量更新，支持大文件传输

## 使用
```shell
git clone https://github.com/huisongyang/fileserver.git
mvn package
```
### windows:
编辑./target/runServer.bat，配置root.dir.path
```bat
@echo off
rem 文件根目录
set root.dir.path="D:\serverdir"

::不扫描文件，分号分隔
set root.dir.exclude=
::不扫描目录，分号分隔
set root.dir.excludeDir=

::默认端口是9999
java -Droot.dir.path=%root.dir.path% -Droot.dir.exclude=%root.dir.exclude% -Droot.dir.excludeDir=%root.dir.excludeDir% -cp fileserver-0.0.1-SNAPSHOT.jar com.yhs.fileserver.server.Server
pause > nul
```
运行runServer.bat

编辑./target/runClient.bat，配置root.dir.path
运行runClient.bat

### linux:
编辑./target/runServer.sh，配置root.dir.path
```shell
#根目录
root_dir_path=/home/weblogic/client

#不扫描文件，分号分隔
root_dir_exclude=
#不扫描目录，分号分隔
root_dir_excludeDir=

#默认端口是9999
java -Droot.dir.path=${root_dir_path} -Droot.dir.exclude=${root_dir_exclude} -Droot.dir.excludeDir=${root_dir_excludeDir} -cp fileserver-0.0.1-SNAPSHOT.jar com.yhs.fileserver.server.Server
```
客户端更新一般在windows下，在windows下配置好runClient.bat的root.dir.path后运行即可。

#Authors
*小松([@小松](https://github.com/huisongyang))
