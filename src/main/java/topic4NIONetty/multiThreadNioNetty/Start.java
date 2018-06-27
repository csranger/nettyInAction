package topic4NIONetty.multiThreadNioNetty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import topic4NIONetty.multiThreadNioNetty.pool.NioSelectorRunnablePool;
/**
 * 启动多线程NIO服务器
 */
public class Start {

	public static void main(String[] args) {

		// 1. 通过两个 线程池 创建线程管理者对象
        // 2. 创建了 1个 boss线程 (处理 ServerSocketChannel 的 selector 线程 = NioServerBoss线程) 和 16 个 Worker线程(SocketChannel
        // 的 selector 线程 = NioServerWorker线程) 放入线程管理者对象的 数组 成员变量内；这些线程含有 线程池 属性，在创建线程对象时就将自身对象
        // 提交到线程池内运行；而这些线程的线程任务是 selector.select 即监控所有注册的 Channel，有需要 IO 操作时，返回 Channel 的数量，同时
        // 将对应的 SelectionKey 加入到被选择的 SelectionKey 中；所有除了main线程外，有一个seector线程监控ServerSocketChannel 的IO操作(boss线程池运行)，
        // 有16个selector线程监控SocketChannel的IO操作(Worker线程池内运行)
		NioSelectorRunnablePool nioSelectorRunnablePool = new NioSelectorRunnablePool(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());




		// 1.虽然启动了selector线程监控 Channel IO 操作；但是selector上并没有注册任何Channel
        // 2.创建一个 ServerSocketChannel，监听指定端口，注册到 boss线程(处理 ServerSocketChannel 的 selector 线程 = NioServerBoss线程)
        // 3.这样的话，main线程执行完毕，只剩下一个boss线程(selector监听ServerSocketChannel的ACCEPT IO 操作，当有新客户端连接会使得ACCEPT IO 操
        // 作准备就绪)和16个worker线程(selector监控已注册SocketChannel的IO操作)
		ServerBootstrap bootstrap = new ServerBootstrap(nioSelectorRunnablePool);
		bootstrap.bind(new InetSocketAddress(10101));


		System.out.println("start");
 	}

}
