# LibDownload
 下载相关引用

#### MqttFactory
mqtt工厂类，用于创建mqtt任务

* getInstance 初始化
* init 初始化,参数包括服务器地址，客户端id，用户名，密码，主题，上线指令，离线指令，重连指令，回调函数

#### MqttListener
mqtt监听类，用于监听mqtt的连接状态

* onConnectSuccess 连接成功
* onConnectFailure 连接失败
* onMessageArrived 接收到消息,主题和消息json字符串
