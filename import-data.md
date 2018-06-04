# 数据导入流程

### 准备
* 在web-monitor.jar所在的目录创建conf子目录，在其中配置global.conf文件，从而设定数据库连接信息
* 在数据库中初始化表结构，详情请见[schema.sql](https://github.com/yangziwen/web-monitor/blob/master/src/main/resources/sql/schema.sql)
* 准备nginx的web_access.log，日志格式如下

```
log_format  main  '$remote_addr - $cookie_bd_x_user - $remote_user [$time_local]'
                  '"$request" $status $body_bytes_sent "$http_referer" '
                  '"$http_user_agent" $request_time $upstream_response_time';
```

### 导入url信息
```
java -jar web-monitor.jar scan-api -p /your/project/path
```

### 导入nginx日志
```
 java -jar web-monitor.jar import-data \
 -ft 2018-05-28 \
 -tt 2018-06-01 \
 -i 30m \
 -tn 4 \
 -f /Users/baidu/nginxlog/web_access.log.20180528 \
 -f /Users/baidu/nginxlog/web_access.log.20180529 \
 -f /Users/baidu/nginxlog/web_access.log.20180530
```