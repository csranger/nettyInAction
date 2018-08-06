package topic4multiThreadNIO.multiThreadNioServer.pool;

import java.nio.channels.SocketChannel;

/**
 * Worker 接口的类就是线程类 NioServerWorker，负责一个 selector 监控 SocketChannel
 */
public interface Worker {

    public void registerNewChannelTask(SocketChannel channel);

}
