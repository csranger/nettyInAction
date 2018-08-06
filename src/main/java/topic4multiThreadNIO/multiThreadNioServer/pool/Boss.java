package topic4multiThreadNIO.multiThreadNioServer.pool;

import java.nio.channels.ServerSocketChannel;
/**
 * Boss 接口的类就是线程类 NioServerBoss，负责一个 selector 监控 ServerSocketChannel
 *
 * 完全可以不使用Boss接口，将这个注册selector的抽象方法写在NioServerBoss类内，selector线程管理者的数组写成NioServerBoss[]
 * Boss 类和 Worker 就是线程类，分别是 NioServerBoss 线程类和 NioServerWorker 线程类，使用接口使得 线程管理者类 和 线程类 解偶
 *
 */
public interface Boss {

	public void registerAcceptChannelTask(ServerSocketChannel serverChannel);
}
