package topic4multiThreadNIO.multiThreadNioServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;

import topic4multiThreadNIO.multiThreadNioServer.pool.NioSelectorRunnablePool;
import topic4multiThreadNIO.multiThreadNioServer.pool.Worker;

/**
 * worker实现类
 */
public class NioServerWorker extends AbstractNioSelector implements Worker {
    private Charset charset = Charset.forName("UTF-8");

    // 初始化 时就意味着将当前线程放入 线程池 运行了
    // 这个线程会阻塞在 selector.select() 方法，当有客户端发送数据才会继续运行 processTask process 方法
    public NioServerWorker(Executor executor, String threadName, NioSelectorRunnablePool selectorRunnablePool) {
        super(executor, threadName, selectorRunnablePool);
    }

    // select 和 process 属于该线程的线程任务，一旦初始化，就放入线程池运行，就会执行这些方法
    // 1. 监控所有注册的 SocketChannel，有需要 IO 操作时，返回 Channel 的数量，同时将对应的 SelectionKey 加入到被选择的 SelectionKey 中。
    // 连接的客户端没有发送数据，此 selector线程 会阻塞在这里
    @Override
    protected int select(Selector selector) throws IOException {
        return selector.select(500);
    }

    // 2. 遍历注册到这个selector线程上的并且 READ IO 操作已准备就绪的 SocketChannel，读取客户端数据，回写数据
    // 连接的客户端发送数据才会使得 SocketChannel READ IO 操作准备就像，才执行到这个方法
    @Override
    protected void process(Selector selector) throws IOException {
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        if (selectedKeys.isEmpty()) {
            return;
        }
        Iterator<SelectionKey> ite = getSelector().selectedKeys().iterator();
        while (ite.hasNext()) {
            SelectionKey key = (SelectionKey) ite.next();
            // 移除，防止重复处理
            ite.remove();

            // 得到事件发生的Socket通道
            SocketChannel channel = (SocketChannel) key.channel();

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            if (channel.read(buffer) > 0) {
                buffer.flip();
                String msg = charset.decode(buffer) + "";
                System.out.println("服务端收到消息：" + msg);
                buffer.clear();

                // 回写数据：实现多人聊天功能
                // 获取所有线程上的selector，从而获取注册在selector上的SocketChannel
                Worker[] workers = getSelectorRunnablePool().getWorkers();
                for (Worker worker : workers) {
                    for (SelectionKey sk : ((NioServerWorker) worker).getSelector().keys()) {
                        SocketChannel target = (SocketChannel) sk.channel();
                        if (target != channel) target.write(charset.encode(msg));
                    }
                }
            } else {
                System.out.println("客户端断开连接");
                key.cancel();
            }
        }
    }


    // boss线程的ServerSocketChannel获取得到的SocketChannel，注册到SocketChannel所在线程的selector
    // 将这个任务放入这个线程的任务队列中等待执行，
    // 实现 Worker 接口的方法
    public void registerNewChannelTask(final SocketChannel channel) {
        final Selector selector = getSelector();
        registerTask(new Runnable() {
            @Override
            public void run() {
                try {
                    //将客户端注册到selector中
                    channel.register(selector, SelectionKey.OP_READ);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
