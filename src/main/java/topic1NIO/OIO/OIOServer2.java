package topic1NIO.OIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 1-2
 * 修改传统的Socket服务器代码：可以为多个客户端处理消息
 * 客户端：线程数＝M:N   (客户端数为M时，连接池线程数量始终为N)    同步阻塞 IO
 * 1）为何阻塞，关键在于read时是否立马返回，而不是关注 accept是否阻塞； 线程等待直到io请求所需的资源准备好那么这个就是阻塞。如果线程不等待，那么就是非阻塞
 * 2）为何称同步：实际的IO操作由应用程序本身去执行，会阻塞线程，就是同步IO
 */

public class OIOServer2 {

    public static void main(String[] args) throws Exception {
        // 线程池
        ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();

        // 创建socket服务，监听10101端口
        ServerSocket server = new ServerSocket(10101);
        System.out.println("服务器启动！");
        while (true) {
            // 获取一个套接字（阻塞）
            final Socket socket = server.accept();
            System.out.println("来了一个新客户端！");
            // 业务处理
            newCachedThreadPool.execute(new Runnable() {
                public void run() {
                    handler(socket);
                }
            });
        }
    }

    /**
     * 业务处理：读取数据；作为线程任务
     *
     * @param socket
     * @throws Exception
     */
    public static void handler(Socket socket) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String content = "";
            // 读取数据（阻塞），客户端如果不发送数据会阻塞在这里
            while ((content = br.readLine()) != null) {
                System.out.println(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println("socket关闭");
                socket.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}

/**
 * 理解：系统启动，使用ServerSocket.accept (餐厅大门) 监听客户端的 socket (客人) ，每来一个
 * socket (客人) ，使用一个线程 (服务员) 进行业务处理 (线程任务) ，可见每个线程 (服务员) 只能服务
 * 一个socket (客人)，造成资源大量消耗。不适合作为长连接服务器，适合短连接。
 */
