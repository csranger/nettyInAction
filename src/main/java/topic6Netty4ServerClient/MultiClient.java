package topic6Netty4ServerClient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 但客户端多连接
 */
public class MultiClient {
    private final AtomicInteger index = new AtomicInteger();


    // 服务类
    private Bootstrap bootstrap = new Bootstrap();


    // 使用数组缓存所有获取到的连接
    private List<Channel> channels = new ArrayList<>();


    // 初始化多少条会话
    public void init(int count) {
        EventLoopGroup worker = new NioEventLoopGroup();

        // 设置线程池
        bootstrap.group(worker);

        //  设置socket工厂
        bootstrap.channel(NioSocketChannel.class);

        // 设置管道
        bootstrap.handler(new ChannelInitializer<io.netty.channel.Channel>() {
            @Override
            protected void initChannel(io.netty.channel.Channel ch) throws Exception {
                ch.pipeline().addLast(new StringDecoder());
                ch.pipeline().addLast(new StringEncoder());
                ch.pipeline().addLast(new ClientHandler());
            }
        });

        // 连接服务端
        for (int i = 0; i <= count; i++) {
            ChannelFuture future = bootstrap.connect("127.0.0.1", 10101);
            channels.add(future.channel());
        }
        System.out.println("client start");

    }

    // 获取回话
    public Channel nextChannel() {
        return getFirstActiveChannel(0);
    }

    // 连接的可用的会话
    // 参照topic4里的方法
    private Channel getFirstActiveChannel(int count) {
        Channel channel = channels.get(Math.abs(index.getAndIncrement() % channels.size()));
        if (!channel.isActive()) {
            // 重连
            reconnect(channel);
            if (count > channels.size()) {
                throw new RuntimeException("No can use channel");
            }
            return getFirstActiveChannel(count + 1);
        }
        return channel;
    }

    // 重连，这里使用锁，不太好，最好使用一个单任务的队列，如果发现连接已经断开，就扔一个任务到这个单任务队列里执行重连
    private void reconnect(Channel channel) {
        synchronized (channel) {
            if (channels.indexOf(channel) == -1) {
                return;
            }
            Channel newChannel = bootstrap.connect("127.0.0.1", 10101).channel();
            channels.set(channels.indexOf(channel), newChannel);
        }
    }


    // 运行测试这个多连接的客户端程序
    public static void main(String[] args) {
        MultiClient client = new MultiClient();
        client.init(5);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.println("请输入：");
                String msg = br.readLine();
                client.nextChannel().writeAndFlush(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
