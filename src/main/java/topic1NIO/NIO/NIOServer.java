package topic1NIO.NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * 1-3 NIO 实现多人聊天室功能
 * NIO服务器  客户端：线程数＝M:1     非阻塞同步IO
 * 1）为何非阻塞，关键在于read时是否立马返回，而不是关注 accept是否阻塞
 * 2）为何称同步：实际的IO操作由应用程序本身去执行，会阻塞线程，就是同步IO
 * <p>
 * 而说java nio提供了异步处理，这个异步应该是指编程模型上的异步。基于reactor模式的事件驱动，事件处理器的注册和处理器的执行是异步的。
 * <p>
 * 阻塞非阻塞？同步异步？：
 * IO操作分成两步，一是发出io请求，二是完成实际IO操作。如果发出IO请求(看read方法)会阻塞线程，就是阻塞IO
 * 如果实际的IO操作由操作系统完成，再将结果返还给应用程序，这就是异步IO，如果实际的IO操作由应用程序完成，会阻塞线程，就是同步IO
 */
public class NIOServer {

    private Selector selector;
    Charset charset = Charset.forName("UTF-8");

    private void initServer() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress("127.0.0.1", 10101));

        // 注册到 selector
        serverSocketChannel.configureBlocking(false);
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void listen() throws IOException {
        System.out.println("服务器启动成功");

        // 轮询
        while (true) {
            // 监控所有注册的 Channel，有需要 IO 操作时，返回 Channel 的数量，同时将对应的 SelectionKey 加入到被选择的 SelectionKey 中。

            // 全过程：ServerSocketChannel 监控端口，同时将此 此 Channel 注册到 selector，注册时提供 SelectionKey.OP_ACCEPT 表示
            // ServerSocketChannel 对 ACCEPT IO 操作感兴趣，除非 SelectionKey.cancel(相当于de-register)，不需再次说明此 Channel 对什
            // 么 IO 操作感兴趣。一旦有客户端请求连接，则此 ServerSocketChannel 的 ACCEPT IO操作准备就绪，ServerSocketChannel 会进入
            // selctor.selectedKeys集合，表示ServerSocketChannel 需要进行 ACCEPT IO 操作。 获取到 ServerSocketChannel 注册的信息 SelectionKey
            // 后将其从selctor.selectedKeys集合中删除，因为 ServerSocketChannel的 ACCEPT IO操作准备就绪(且对ACCEPT感兴趣) 即
            // (readyOps() & OP_ACCEPT) != 0，所以 isAcceptable，从而获取 SocketChannel，注册到 selector，注册时提供 SelectionKey.OP_READ
            // 表示对该SocketChannel 的 RAED IO 操作感兴趣如果，SocketChannel没有发送消息，即IO操作未准备就绪，则selector.select
            // 返回0，阻塞在selector.select方法这里，当有客户端发送数据，RAED IO 操作准备就绪，进行handlerRead IO操作，因为发送了数据，所以不会进行
            // else 中语句，即不会显示客户端关闭，当客户端关闭，会触发 RAED IO 操作准备就绪，因为没有发送数据，所以进行else中语句，打印客户端关闭。

            // 断点调试解决一切疑惑！使用 telnet 作为客户端

            // select：监控所有注册的 Channel，当它们中间有需要处理的 IO 操作时，该方法返回需要IO操作的Channel数量，同时将对应的SelectionKey加入selctor.selectedKeys集合
            selector.select();
            // selectedKeys 返回 SelectionKey 集合，代表通过 selector.select 方法获取的需要进行 IO 操作的 Channel
//            System.out.println(selector.selectedKeys().size());
            // SelectionKey 代表 SelectableChannel 和 Selector 间的注册关系，可通过 SelectionKey 对象获取对应的 Channel 和 Selector
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                handler(selectionKey);
            }

        }

    }

    // 在 selector 上注册过的 Channel，当需要进行 IO 操作时，对其进行处理b
    public void handler(SelectionKey sk) throws IOException {
        if (sk.isAcceptable()) {       // (readyOps() & OP_ACCEPT) != 0   此注册的 Channel 对 Accept IO 操作感兴趣 + 此 Accept IO 操作准备就绪
            handlerAccept(sk);
        } else if (sk.isReadable()) {  // (readyOps() & OP_READ) != 0
            handlerRead(sk);
        }
    }

    private void handlerAccept(SelectionKey sk) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) sk.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        System.out.println("新的客户端连接");
        socketChannel.register(selector, SelectionKey.OP_READ);  // SocketChannel 不支持 OP_ACCEPT 只支持 READ WRITE CONNECT
    }

    private void handlerRead(SelectionKey sk) throws IOException {
        SocketChannel socketChannel = (SocketChannel) sk.channel();

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        if (socketChannel.read(buffer) > 0) {
            buffer.flip();
            String msg = charset.decode(buffer).toString();
            System.out.println("服务端收到消息：" + msg);
            buffer.clear();

            // 向其他客户端即SocketChannel回写数据:实现多人聊天室功能
            for (SelectionKey key : selector.keys()) {
                SelectableChannel target = key.channel();
                if (target instanceof SocketChannel) {
                    SocketChannel targetSocket = (SocketChannel) target;
                    if (targetSocket != socketChannel) targetSocket.write(charset.encode(msg));
                }
            }

        } else {
            System.out.println("客户端断开连接");
            sk.cancel();           // 相当于de-register
        }
    }


    public static void main(String[] args) throws IOException {
        NIOServer server = new NIOServer();
        server.initServer();
        server.listen();
    }
}
