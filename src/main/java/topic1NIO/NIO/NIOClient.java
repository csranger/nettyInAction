package topic1NIO.NIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * 客户端只有一个SocketChannel注册到selector
 * 和 NIOServer 相比多了个主动想服务端发送数据，所以需要另一个线程实时接收数据，实时接收数据和 NIOServer 基本一样
 */
public class NIOClient {
    private Selector selector;
    private Charset charset = Charset.forName("UTF-8");
    private SocketChannel socketChannel;

    private void initClient() throws IOException {
        socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 10101));

        // 注册到 selector
        socketChannel.configureBlocking(false);
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private void connect() {
        System.out.println("客户端启动成功");

        // 启动一个线程接收服务器端发来的数据
        new ClientThread().start();

        // 客户端主动向服务端发送数据
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) { // 字节输入流 -> 字符输入流 -> 包装更好方法的字符输入流
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                socketChannel.write(charset.encode(line));
                if (line.equals("exit")) {
                    System.exit(1);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    // 读取服务器端数据的线程
    private class ClientThread extends Thread {

        @Override
        public void run() {
            try {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();
                    handler(selectionKey);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    // 在 selector 上注册过的 Channel，当需要进行 IO 操作时，对其进行处理b
    private void handler(SelectionKey selectionKey) throws IOException {
        if (selectionKey.isReadable()) handlerRead(selectionKey);
    }

    private void handlerRead(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        if (socketChannel.read(buffer) > 0) {
            buffer.flip();
            String msg = charset.decode(buffer).toString();
            System.out.println(msg);
            buffer.clear();

        } else {
            System.out.println("服务端关闭");
        }
    }

    public static void main(String[] args) throws IOException {
        NIOClient client = new NIOClient();
        client.initClient();
        client.connect();
    }

}
