@echo off
rem 文件根目录
set root.dir.path="D:\serverdir"

::不扫描文件，分号分隔
set root.dir.exclude=
::不扫描目录，分号分隔
set root.dir.excludeDir=

::默认端口是9999
java -Droot.dir.path=%root.dir.path% -Droot.dir.exclude=%root.dir.exclude% -Droot.dir.excludeDir=%root.dir.excludeDir% -cp fileserver-0.0.1-SNAPSHOT.jar com.yhs.fileserver.server.Server
