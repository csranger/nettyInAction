package topic1NIO.OIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 1-1
 * 传统的Socket服务器
 * 又称BIO  客户端：线程数＝1:1   阻塞同步IO
 * 如何判断阻塞非阻塞，同步异步？见NIOServer.java
 */

public class OIOServer {

    public static void main(String[] args) throws Exception {
        // 创建socket服务，监听10101端口
        ServerSocket server = new ServerSocket(10101);
        System.out.println("服务器启动！");
        while (true) {
            // 阻塞点1：获取一个套接字（阻塞）如果没有客户端连接进来，会阻塞在这里
            final Socket socket = server.accept();
            System.out.println("来了一个新客户端！");
            // 业务处理
            handler(socket);
        }
    }

    /**
     * 业务处理：读取数据
     * @param socket
     * @throws Exception
     */
    public static void handler(Socket socket) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String content = "";
            // 阻塞点2：读取数据（阻塞），客户端连接后，如果没有发送数据，会阻塞在这里
            // 判断 IO 是否阻塞，关键在于read时是否立马返回，而不是关注 accept是否阻塞
            while ((content = br.readLine()) != null) {
                System.out.println(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("socket关闭");
        }
    }
}
