# Raven: 产品级别 IM 服务搭建底层框架。

## 使用要求
* JDK 版本：1.8+ 
* 依赖类库：
  * [slf4j-log4j12 1.7.5+](https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12)
  * [naivemonitor 1.0+](https://github.com/heimuheimu/naivemonitor)

## 使用限制
* Raven 框架并没有定义消息协议，由使用方自行定义，使用方通过实现 IMClientListener、IMClientInterceptor 接口来满足自身业务需求。

## Raven 特色：
* 使用 Nio 实现，通常单台服务器应支撑 1W - 10W 长链接。
* 限制每个链接单次读、写的最大字节数，防止单链接异常，造成整体消息延迟。 
* 完善的日志信息、监控信息，方便问题定位。

## Maven 配置
```xml
    <dependency>
        <groupId>com.heimuheimu</groupId>
        <artifactId>raven</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
```

## Log4J 配置
```
# Raven 根日志
log4j.logger.com.heimuheimu.raven=WARN, RAVEN
log4j.additivity.com.heimuheimu.raven=false
log4j.appender.RAVEN=org.apache.log4j.DailyRollingFileAppender
log4j.appender.RAVEN.file=${log.output.directory}/raven/raven.log
log4j.appender.RAVEN.encoding=UTF-8
log4j.appender.RAVEN.DatePattern=_yyyy-MM-dd
log4j.appender.RAVEN.layout=org.apache.log4j.PatternLayout
log4j.appender.RAVEN.layout.ConversionPattern=%d{ISO8601} %-5p [%F:%L] : %m%n

# IM 客户端日志信息
log4j.logger.RAVEN_IM_CLIENT_LOG=INFO, RAVEN_IM_CLIENT_LOG
log4j.additivity.RAVEN_IM_CLIENT_LOG=false
log4j.appender.RAVEN_IM_CLIENT_LOG=org.apache.log4j.DailyRollingFileAppender
log4j.appender.RAVEN_IM_CLIENT_LOG.file=${log.output.directory}/raven/im_client.log
log4j.appender.RAVEN_IM_CLIENT_LOG.encoding=UTF-8
log4j.appender.RAVEN_IM_CLIENT_LOG.DatePattern=_yyyy-MM-dd
log4j.appender.RAVEN_IM_CLIENT_LOG.layout=org.apache.log4j.PatternLayout
log4j.appender.RAVEN_IM_CLIENT_LOG.layout.ConversionPattern=%d{ISO8601} %-5p [%F:%L] : %m%n

# IM 客户端管理器日志信息
log4j.logger.RAVEN_IM_CLIENT_MANAGER_LOG=INFO, RAVEN_IM_CLIENT_MANAGER_LOG
log4j.additivity.RAVEN_IM_CLIENT_MANAGER_LOG=false
log4j.appender.RAVEN_IM_CLIENT_MANAGER_LOG=org.apache.log4j.DailyRollingFileAppender
log4j.appender.RAVEN_IM_CLIENT_MANAGER_LOG.file=${log.output.directory}/raven/im_client_manager.log
log4j.appender.RAVEN_IM_CLIENT_MANAGER_LOG.encoding=UTF-8
log4j.appender.RAVEN_IM_CLIENT_MANAGER_LOG.DatePattern=_yyyy-MM-dd
log4j.appender.RAVEN_IM_CLIENT_MANAGER_LOG.layout=org.apache.log4j.PatternLayout
log4j.appender.RAVEN_IM_CLIENT_MANAGER_LOG.layout.ConversionPattern=%d{ISO8601} %-5p [%F:%L] : %m%n

# iM 客户端超时扫描任务执行日志信息
log4j.logger.RAVEN_IM_CLIENT_TIMEOUT_LOG=INFO, RAVEN_IM_CLIENT_TIMEOUT_LOG
log4j.additivity.RAVEN_IM_CLIENT_TIMEOUT_LOG=false
log4j.appender.RAVEN_IM_CLIENT_TIMEOUT_LOG=org.apache.log4j.DailyRollingFileAppender
log4j.appender.RAVEN_IM_CLIENT_TIMEOUT_LOG.file=${log.output.directory}/raven/im_client_timeout.log
log4j.appender.RAVEN_IM_CLIENT_TIMEOUT_LOG.encoding=UTF-8
log4j.appender.RAVEN_IM_CLIENT_TIMEOUT_LOG.DatePattern=_yyyy-MM-dd
log4j.appender.RAVEN_IM_CLIENT_TIMEOUT_LOG.layout=org.apache.log4j.PatternLayout
log4j.appender.RAVEN_IM_CLIENT_TIMEOUT_LOG.layout.ConversionPattern=%d{ISO8601} %-5p [%F:%L] : %m%n
```

## Spring 配置
```xml
    <!-- IM 服务端配置信息 -->
    <bean id="ravenServerConfiguration" class="com.heimuheimu.raven.IMServerConfiguration">
        <property name="port" value="4182" /> <!-- 监听端口 -->
        <property name="socketConfiguration"> <!-- IMServer 与 IM 客户端连接的 {@link SocketChannel} 使用的 Socket 配置信息 -->
            <bean class="com.heimuheimu.raven.net.SocketConfiguration">
                <property name="keepAlive" value="false" />
                <property name="tcpNoDelay" value="false" />
                <property name="sendBufferSize" value="32768" />
                <property name="receiveBufferSize" value="16384" />
            </bean>
        </property>
        <property name="clientListener" ref="demoRavenIMClientListener" /> <!-- IMClientListener 实现类，由使用方自行实现 -->
        <property name="clientTimeout" value="60" /> <!-- IM 客户端超时时间，单位：秒，如果小于等于 0，则不会超时，默认为 60 秒 -->
        <property name="poolSize" value="20" /> <!-- IMServer 使用 IM 客户端管理器数量，默认为 20，如果小于等 0，则使用具体实现指定的默认值 -->
        <property name="clientManagerConfiguration"> <!-- IM 客户端管理器使用的配置信息 -->
            <bean class="com.heimuheimu.raven.clients.IMClientManagerConfiguration">
                <property name="capacity" value="10000" /> <!-- 单个管理器可管理的最大 IM 客户端数量，如果小于等于 0 ，则没有数量限制，默认为 -1 -->
            </bean>
        </property>
        <property name="clientManagerListListener"> <!-- IM 客户端管理器列表事件监听器 -->
            <bean class="com.heimuheimu.raven.clients.support.NoticeableIMClientManagerListListener">
                <constructor-arg index="0" value="your-project-name" /> <!-- 当前项目名称 -->
                <constructor-arg index="1" ref="notifierList" /> <!-- 报警器列表，报警器的信息可查看 naivemonitor 项目 -->
            </bean>
        </property>
        <property name="clientInterceptor" ref="demoRavenIMClientInterceptor" /> <!-- IMClientInterceptor 实现类，由使用方自行实现 -->
    </bean>
    
    <!-- IM 服务端 -->
    <bean id="ravenIMServer" class="com.heimuheimu.raven.IMServer" init-method="init" destroy-method="close">
        <constructor-arg index="0" ref="ravenServerConfiguration" />
    </bean>
``` 

## Falcon 监控数据上报 Spring 配置
```xml
    <!-- 监控数据采集器列表 -->
    <util:list id="falconDataCollectorList">
        <!-- Raven 信息监控 -->
        <bean class="com.heimuheimu.raven.monitor.falcon.IMClientManagerDataCollector" />
        <bean class="com.heimuheimu.raven.monitor.falcon.IMClientDataCollector" />
        <bean class="com.heimuheimu.raven.monitor.falcon.ByteMessageDataCollector" />
    </util:list>
    
    <!-- Falcon 监控数据上报器 -->
    <bean id="falconReporter" class="com.heimuheimu.naivemonitor.falcon.FalconReporter" init-method="init" destroy-method="close">
        <constructor-arg index="0" value="http://127.0.0.1:1988/v1/push" /> <!-- Falcon 监控数据推送地址 -->
        <constructor-arg index="1" ref="falconDataCollectorList" />
    </bean>
```

## Falcon 上报数据项说明（上报周期：30秒）
### IM 客户端数据项：
 * raven_client_established_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 当前保持连接的 IM 客户端数量
 * raven_client_created_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内创建的 IM 客户端数量
 * raven_client_closed_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内关闭的 IM 客户端数量
 * raven_client_established_error_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 IM 客户端创建失败的次数
 * raven_client_closed_error_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 IM 客户端关闭失败的次数
 * raven_client_timeout_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内发生超时错误的 IM 客户端数量
  
### 字节消息发送数据项：
 * raven_byte_message_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内需要发送的消息总数
 * raven_byte_message_total_byte_length/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内需要发送的字节总长度
 * raven_byte_message_avg_byte_length/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内需要发送的单条消息平均字节长度
 * raven_byte_message_max_byte_length/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内需要发送的单条消息最大字节长度
 * raven_byte_message_error_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内发送失败的消息总数
 * raven_byte_message_sent_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内发送成功的消息总数
 * raven_byte_message_sent_avg_delay/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内发送成功的消息平均延迟时间，单位：毫秒
 * raven_byte_message_sent_max_delay/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内发送成功的消息最大延迟时间，单位：毫秒 

### IM 客户端管理器数据项：
 * raven_manager_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 当前可用的 IM 客户端管理器数量
 * raven_manager_avg_client_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 当前每个 IM 客户端管理器已管理的 IM 客户端平均数量
 * raven_manager_max_client_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 当前单个 IM 客户端管理器已管理的 IM 客户端最大数量
 * raven_manager_readable_client_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内可读的 IM 客户端数量
 * raven_manager_writable_client_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内可写的 IM 客户端数量

### IM 客户端管理器执行数据项：
 * raven_manager_tps/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 迭代所有可用的 IM 客户端方法在 30 秒内每秒平均执行次数
 * raven_manager_peak_tps/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 迭代所有可用的 IM 客户端方法在 30 秒内每秒最大执行次数
 * raven_manager_avg_exec_time/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 迭代所有可用的 IM 客户端方法在 30 秒内单次操作平均执行时间
 * raven_manager_max_exec_time/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 迭代所有可用的 IM 客户端方法在 30 秒内单次操作最大执行时间
 * raven_manager_register_error_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内遇到的 IM 客户端注册失败错误次数
 * raven_manager_communicate_error_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内遇到的 IM 客户端通信失败错误次数
 * raven_manager_select_error_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内遇到的 IM 客户端选择失败错误次数
 
### IM 客户端管理器 Socket 数据项：
 * raven_manager_socket_read_bytes/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 读取的总字节数
 * raven_manager_socket_avg_read_bytes/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 每次读取的平均字节数
 * raven_manager_socket_max_read_bytes/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 单次读取的最大字节数
 * raven_manager_socket_written_bytes/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 写入的总字节数
 * raven_manager_socket_avg_written_bytes/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 每次写入的平均字节数
 * raven_manager_socket_max_written_bytes/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 单次写入的最大字节数
 
## 更多信息
* [NaiveMonitor 项目主页](https://github.com/heimuheimu/naivemonitor)