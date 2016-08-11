# fileserver
一个基于netty的文件服务器，可以用于分发应用程序，文件md5校验比对差量更新，支持大文件传输

##使用
```shell
git https://github.com/huisongyang/fileserver.git
mvn package
cd ./target
#启动服务端
vi runServer.sh #配置root.dir.path=你需要更新的文件目录
./runServer.sh

#启动客户端更新
vi runClient.bat #配置root.dir.path=你的应用程序目录
call runClient.bat
```

#Authors
*小松([@小松](https://github.com/huisongyang))
