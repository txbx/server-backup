由于甲骨文云有玄学封号的风险，因此建了这个项目，用于将服务器上的文件定时备份到谷歌云盘中

## 功能：
- 备份指定文件夹、文件并压缩上传到谷歌云盘

## 使用方式
1. 安装docker: 
```shell
curl -fsSL https://get.docker.com | sh
```

2. 运行：
```shell
docker run -d 
-v /etc/localtime:/etc/localtime \
-v 需要备份的文件或者目录:/home/server-backup \
-v 谷歌云盘服务账号的密钥文件:/home/auth/auth.json \
-e SERVER_NAME="保存在谷歌云盘上的文件夹"
-e PARENT="谷歌云盘服务账号所拥有操作权限的文件夹id"
--name backup \
tanxiubiao/server-backup:0.1
```

> 说明
谷歌云盘服务账号的密钥文件的获取方式参见：
PARENT的获取方式参见：

例如：我有一个需要备份的目录以及一个单独的文件 所以我的运行命令就是：
```shell
docker run -d \
-v /etc/localtime:/etc/localtime \ # 运行程序的时区和当前服务器一样，目前默认每天凌晨1点运行
-v /home/txb/docker_data:/home/server-backup \  # /home/txb/docker_data 是我需要备份的目录
-v /home/txb/tcp.sh:/home/server-backup/tcp.sh \  # /home/txb/docker_data 是我需要备份的文件，多个文件或者目录像这样添加就行
-v /home/txb/docker_data/docker-file/server-bk/stoked-bivouac-367509-cbaff8577527.json:/home/auth/auth.json \ # /home/txb/docker_data/docker-file/server-bk/stoked-bivouac-367509-cbaff8577527.json 是我谷歌云盘服务账号的密钥文件
-e PARENT="1tnwC9TV8ZdbnGxaswqwyTwuBjVdFJVz4" \ # 是我账号拥有操作权限的的文件夹id
-e SERVER_NAME="oracle-arm" \ # 备份文件将上传到的文件夹，如果云盘上不存在会自动创建。这个文件夹是 PARENT 文件夹的子目录
--name backup \
tanxiubiao/server-backup:0.1
```
