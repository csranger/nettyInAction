package topic4NIONetty.multiThreadNioNetty;

import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;

import topic4NIONetty.multiThreadNioNetty.pool.Boss;
import topic4NIONetty.multiThreadNioNetty.pool.NioSelectorRunnablePool;

/**
 * 创建一个 ServerSocketChannel，监听指定端口，注册到 boss线程(处理 ServerSocketChannel 的 selector 线程 = NioServerBoss线程)
 */
public class ServerBootstrap {

    private NioSelectorRunnablePool selectorRunnablePool;

    public ServerBootstrap(NioSelectorRunnablePool selectorRunnablePool) {
        this.selectorRunnablePool = selectorRunnablePool;
    }

    // 绑定端口
    public void bind(final SocketAddress localAddress) {
        try {
            // 创建一个 ServerSocketChannel，监听指定端口
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(localAddress);

            // 获取一个boss线程，将创建的 ServerSocketChannel 注册到此 boss线程(处理 ServerSocketChannel 的 selector 线程)
            // 将注册任务放入这个boss线程的任务队列中
            Boss nextBoss = selectorRunnablePool.nextBoss();
            nextBoss.registerAcceptChannelTask(serverChannel);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
