# web-monitor
### 一个基于nginx日志的web接口实时监控系统原型
![](http://upload-images.jianshu.io/upload_images/4565596-99841b265b18664c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 技术选型
- 前端：Vue.js
- 后端：Spark Framework
- 数据库：MySQL
- 定时任务：cron4j
- 日志采集/解析：Filebeat + Logstash

### 配置范例
**1. nginx日志格式配置**
```
log_format  main '$remote_addr - $remote_user [$time_local] "$request" '
              '$status $body_bytes_sent "$http_referer" '
              '"$http_user_agent" "$http_x_forwarded_for" '
              '"$upstream_addr" "$upstream_response_time"';
```
**2. filebeat配置**
```
filebeat.prospectors:
- input_type: log
  paths:
    - /usr/local/Cellar/nginx/1.10.3/logs/access.log
  exclude_files: [".gz$"]
output.logstash:
  hosts: ["localhost:5044"]
```
**3. logstash配置**
```
input {
  beats {
    # The port to listen on for filebeat connections.
    port => 5044
    # The IP address to listen for filebeat connections.
    host => "0.0.0.0"
  }
}
filter {
   grok {
      match => { "message" => ["%{IPORHOST:[nginx][access][remote_ip]} - %{DATA:[nginx][access][user_name]} \[%{HTTPDATE:[nginx][access][timestamp]}\] \"%{WORD:[nginx][access][method]} %{DATA:[nginx][access][url]} HTTP/%{NUMBER:[nginx][access][http_version]}\" %{NUMBER:[nginx][access][response_code]} %{NUMBER:[nginx][access][body_bytes]} \"%{DATA:[nginx][access][referrer]}\" \"%{DATA:[nginx][access][agent]}\" \"%{DATA:[nginx][access][http_x_forwarded_for]}\" \"%{DATA:[nginx][access][upstream]}\" \"%{NUMBER:[nginx][access][response_time]}\""] }
      remove_field => "message"
   }
   useragent {
      source => "[nginx][access][agent]"
      target => "[nginx][access][user_agent]"
      remove_field => "[nginx][access][agent]"
   }
}
output {
  http {
    url => "http://localhost:8050/monitor/nginx/access.json"
    http_method => "post"
  }
}
```
### 启动方法

**1.clone工程**
```
git clone https://github.com/yangziwen/web-monitor.git
cd web-monitor
```

**2. 初始化数据库**
```
create database web_monitor default charset utf8;
grant all on web_monitor.* to 'monitor'@localhost identified by '1234';
flush privileges;
use web_monitor;
source src/main/resources/sql/schema.sql;
```

**3. 打包 & 部署**
- 确认环境中正确安装了java8, maven, npm
- `mvn package`生成web-monitor.jar
- 将web-monitor.jar与项目中的conf目录拷贝到同一个目录中

**4. 启动**
- `java -jar web-monitor.jar server`，然后访问 [http://localhost:8050](http://localhost:8050)
- filebeat和logstash可参考前面的范例按需配置并启动

**5. 开发 & 调试**
- 由于前后端代码需要分离调试，因此本地需要配置并启动nginx，配置如下
```
server {
    listen 8090;
    server_name localhost;
    location ~ ^/monitor.*\.json$ {
        proxy_pass http://127.0.0.1:8050;
    }
    location / {
        proxy_pass http://127.0.0.1:8080;
    }
}
```
- 在目录src/main/frontend中执行`npm run dev`，以开发模式启动前端代码，监听8080端口（如果是第一次启动，请在启动前先执行npm install & npm run init）
- 在IDE中运行io.github.yangziwen.webmonitor.Server中的main方法启动后端代码，监听8050端口（如果仅需调试前端代码，则后端也可按步骤4的方式启动）
- 访问 [http://localhost:8090](http://localhost:8090)，即可按热部署的方式开发和调试前后端代码
