@echo off

rem 配置文件根目录，如果为空，则当前目录为根目录
set root.dir.path="D:\workspace\test"

if "%root.dir.path%"=="" set root.dir.path=%CD%

rem 不删除文件列表，分号分隔
set root.dir.exclude="autoupdate-2.0.0-SNAPSHOT.jar;clientMd5.record;runClient.bat;Client.log"
rem 不删除目录列表，分号分隔
set root.dir.excludeDir="log"

rem 启动更新
java -Droot.dir.path=%root.dir.path% -Droot.dir.exclude=%root.dir.exclude% -Droot.dir.excludeDir=%root.dir.excludeDir% -cp fileserver-0.0.1-SNAPSHOT.jar com.yhs.fileserver.client.Client 127.0.0.1 9999
if "%errorlevel%"=="0" echo [info]: 连接关闭

rem 启动应用
echo done
exit