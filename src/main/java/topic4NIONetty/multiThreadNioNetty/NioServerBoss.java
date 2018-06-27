package topic4NIONetty.multiThreadNioNetty;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;

import topic4NIONetty.multiThreadNioNetty.pool.Boss;
import topic4NIONetty.multiThreadNioNetty.pool.NioSelectorRunnablePool;
import topic4NIONetty.multiThreadNioNetty.pool.Worker;

/**
 * boss实现类 NioServerBoss线程 = boss线程 = 处理 ServerSocketChannel 的 selector 线程
 * NioServerBoss 是线程类，线程任务在 AbstractNioSelector 抽象类的 run 方法内
 * NioServerBoss 的线程任务是 将 ServerSocketChannel 注册到 Selector，客户端连接时获取 SocketChannel 并注册到对应的 Selector
 *
 */
public class NioServerBoss extends AbstractNioSelector implements Boss {

    // 初始化 时就意味着将当前线程放入 线程池 运行了
    // 这个线程会阻塞在 selector.select() 方法，当有客户端链接才会继续运行 processTask process 方法
	public NioServerBoss(Executor executor, String threadName, NioSelectorRunnablePool selectorRunnablePool) {
		super(executor, threadName, selectorRunnablePool);
	}


	// select 和 process 属于该线程的线程任务，一旦初始化，就放入线程池运行，就会执行这些方法
    // 1. 监控所有注册的 Channel，有需要 IO 操作时，返回 Channel 的数量，同时将对应的 SelectionKey 加入到被选择的 SelectionKey 中。
    // 没有客户端连接时这个 selector线程 会阻塞在这里
    @Override
    protected int select(Selector selector) throws IOException {
        return selector.select();
    }

	// 2. 遍历注册到 selector 上的并且 ACCEPT IO 操作已经准备就绪的 ServerSocketChannel，获取 SocketChannel 并注册到selector
    // 有客户端连接时才会使得 ServerSocketChannel ACCEPT IO 操作准备就绪，才会执行到这个方法
	@Override
	protected void process(Selector selector) throws IOException {
		Set<SelectionKey> selectedKeys = selector.selectedKeys();
        if (selectedKeys.isEmpty()) {
            return;
        }
        
        for (Iterator<SelectionKey> i = selectedKeys.iterator(); i.hasNext();) {
            SelectionKey key = i.next();
            i.remove();
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
    		// 新客户端 SocketChannel
    		SocketChannel channel = server.accept();
    		// 设置为非阻塞
    		channel.configureBlocking(false);

    		// 找到合适的 selector(服务员)
            // Worker线程 = 处理 SocketChannel 的 selector 线程 = NioWorkerBoss线程
    		Worker nextworker = getSelectorRunnablePool().nextWorker();
    		// boss线程通过ServerSocketChannel获取一个SocketChannel，需要将其注册到此SocketChannel所在线程的selector上即worker线程
            // 因为是两个线程，为了不互相干扰，将添加
    		nextworker.registerNewChannelTask(channel);
    		
    		System.out.println("新客户端链接");
        }
	}


	// 实现 Boss 接口的唯一方法：注册 ServerSocketChannel 到 selector
    public void registerAcceptChannelTask(final ServerSocketChannel serverChannel){
        final Selector selector = getSelector();
        registerTask(new Runnable() {
            @Override
            public void run() {
                try {
                    //注册serverChannel到selector
                    serverChannel.register(selector, SelectionKey.OP_ACCEPT);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
