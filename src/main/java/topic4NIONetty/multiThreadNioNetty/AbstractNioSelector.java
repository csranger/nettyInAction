package topic4NIONetty.multiThreadNioNetty;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import topic4NIONetty.multiThreadNioNetty.pool.NioSelectorRunnablePool;

/**
 * 抽象selector线程类
 */
public abstract class AbstractNioSelector implements Runnable {

	// 线程有 线程池 属性，用于确定这个线程任务属于哪个线程池
	private final Executor executor;


	//  每个线程有一个 selector 负责轮询处理 SocketChannel，理解成  "服务员"
	private Selector selector;

	/**
	 * 选择器wakenUp状态标记
	 */
	private final AtomicBoolean wakenUp = new AtomicBoolean();


	// 当前线程需要处理的任务放入这个先进先出队列，依次处理任务
	private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<Runnable>();


	// 线程名
	private String threadName;
	
	// 线程有 线程管理者 属性
	protected NioSelectorRunnablePool selectorRunnablePool;

	// 构造器1
	AbstractNioSelector(Executor executor, String threadName, NioSelectorRunnablePool selectorRunnablePool) {
		this.executor = executor;
		this.threadName = threadName;
		this.selectorRunnablePool = selectorRunnablePool;
		openSelector();
	}

	// 构造器1.1
    // 非常重要：线程池就是在这里使用的
    // 将当前线程放入线程池运行！！！
    // 线程任务：对于处理 ServerSocketChannel 的 boss 线程/selector 是selector.select()即监控ServerSocketChannel是否有 ACCEPT IO 操作
	private void openSelector() {
		try {
			this.selector = Selector.open();
		} catch (IOException e) {
			throw new RuntimeException("Failed to create a selector.");
		}
		executor.execute(this);
	}

	// 构造器1.1
    // 线程任务
	@Override
	public void run() {
		
		Thread.currentThread().setName(this.threadName);

		while (true) {
			try {
				wakenUp.set(false);

				select(selector);

				processTaskQueue();

				process(selector);
			} catch (Exception e) {
				// ignore
			}
		}

	}

	// 构造器1.1.1
	// 执行队列里的任务
	private void processTaskQueue() {
		for (;;) {
			final Runnable task = taskQueue.poll();
			if (task == null) {
				break;
			}
			task.run();
		}
	}

	// 构造器1.1.2
    // 监控所有注册的 Channel，有需要 IO 操作时，返回 Channel 的数量，同时将对应的 SelectionKey 加入到被选择的 SelectionKey 中。
	protected abstract int select(Selector selector) throws IOException;

	// 构造器1.1.3
    // 对于 bossSelector  ：遍历注册到 selector 上的 ServerSocketChannel 获取 SocketChannel 并注册
    // 对于 workerSelector：将当前 selector 线程下的所有需要处理的 SocketChannel，进行处理，读取客户端数据，回写数据
	protected abstract void process(Selector selector) throws IOException;


    // 向队列中添加任务:这个任务是将 Channel注册到selector上
    protected final void registerTask(Runnable task) {
        taskQueue.add(task);

        Selector selector = this.selector;

        if (selector != null) {
            if (wakenUp.compareAndSet(false, true)) {
                selector.wakeup();
            }
        } else {
            taskQueue.remove(task);
        }
    }





    // getter 方法
    // 通过线程对象获取 selector线程管理者
    public NioSelectorRunnablePool getSelectorRunnablePool() {
        return selectorRunnablePool;
    }

    public Selector getSelector() {
        return selector;
    }

}
