#根目录
root_dir_path=/home/weblogic/client

#不扫描文件，分号分隔
root_dir_exclude=
#不扫描目录，分号分隔
root_dir_excludeDir=

#默认端口是9999
java -Droot.dir.path=${root_dir_path} -Droot.dir.exclude=${root_dir_exclude} -Droot.dir.excludeDir=${root_dir_excludeDir} -cp autoupdate-2.0.0-SNAPSHOT.jar com.yhs.autoupdate.server.Server
