package topic4NIONetty.multiThreadNioNetty.pool;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import topic4NIONetty.multiThreadNioNetty.NioServerBoss;
import topic4NIONetty.multiThreadNioNetty.NioServerWorker;

/**
 * selector线程管理者
 * 核心就是使用数组保存了所有的线程/selector，目的就是在新来一个客户端SocketChannel时轮流注册到下一个selector/Worker线程上
 */
public class NioSelectorRunnablePool {

    // 数组元素是 NioServerBoss 线程(看成selector线程)，负责接收客户端链接，元素数量为 1
    private final AtomicInteger bossIndex = new AtomicInteger();
    private Boss[] bosses;

    // 数组元素是 NioServerWorker 线程对象(看成selector线程)，负责处理需要IO操作的SocketChannel，元素数量为 16
    private final AtomicInteger workerIndex = new AtomicInteger();
    private Worker[] workers;


    // 构造器 1
    public NioSelectorRunnablePool(Executor boss, Executor worker) {
        initBoss(boss, 1);
        initWorker(worker, 1);  //Runtime.getRuntime().availableProcessors() * 2
    }

    // 构造器 1.1
    // 初始化1个boss线程放入bosses数组(boss线程 = 处理 ServerSocketChannel 的 selector 线程 = NioServerBoss线程)："大门迎接客人"
    // 初始化boss线程时就意味着:1.创建一个NioServerBoss对象，初始化其属性(线程池；线程名；线程管理者)；创建selector
    //                       2.将当前线程放入 boss线程池 运行；boss线程的任务是:
    //                          2.1 selector.select，无客户端发起连接会阻塞在这里
    //                          2.2 依次运行线程队列属性里的任务
    //                          2.3 有客户端连接时 selector.select 不阻塞，使用ServerSocketChannel获取SocketChannel，并将其注册在合适
    // 的Worker线程/处理 SocketChannel 的selector上，这个合适的selector 通过selector线程管理者方法来确定！！这也是selector线程管理者为什么使用
    // 数组存储所有的线程，因为方便确定下一个线程。使用nextWorker方法确定下一个Worker线程/处理 SocketChannel 的 selector 线程
    private void initBoss(Executor boss, int count) {
        this.bosses = new NioServerBoss[count];
        for (int i = 0; i < bosses.length; i++) {
            bosses[i] = new NioServerBoss(boss, "boss thread " + (i + 1), this);
        }

    }

    // 构造器 1.2
    // 初始化16个worker线程放入workeres数组(worker线程 = 处理 SocketChannel 的 selector 线程 = NioServerWorker线程)：在数组中新建16个线程(16个selector)，"16个服务员服务客人"
    // 初始化Worker线程时就意味着:1.创建一个NioServerWorker对象，初始化其属性(线程池；线程名；线程管理者)；创建selector
    //                         2.将当前线程放入 worker 线程池 运行；worker 线程的任务是:
    //                            2.1 selector.select，无客户端发起连接会阻塞在这里
    //                            2.2 依次运行线程队列属性里的任务
    //                            2.3 有客户端连接时 selector.select 不阻塞，使用ServerSocketChannel获取SocketChannel，并将其注册在合适
    // 的Worker线程/处理 SocketChannel 的selector上，这个合适的selector 通过selector线程管理者方法来确定！！这也是selector线程管理者为什么使用
    // 数组存储所有的线程，因为方便确定下一个线程。使用nextWorker方法确定下一个Worker线程/处理 SocketChannel 的 selector 线程
    private void initWorker(Executor worker, int count) {
        this.workers = new NioServerWorker[count];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new NioServerWorker(worker, "worker thread " + (i + 1), this);
        }
    }


    // selector线程管理者核心方法！！！将获取的SocketChannel注册到下一个selector/Worker线程上
    public Worker nextWorker() {
        return workers[Math.abs(workerIndex.getAndIncrement() % workers.length)];

    }


    public Boss nextBoss() {
        return bosses[Math.abs(bossIndex.getAndIncrement() % bosses.length)];
    }

    // getter方法
    public Boss[] getBosses() {
        return bosses;
    }

    public Worker[] getWorkers() {
        return workers;
    }
}
