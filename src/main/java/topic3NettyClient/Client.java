package topic3NettyClient;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {

    public static void main(String[] args) {

        // 服务类
        ClientBootstrap bootstrap = new ClientBootstrap();

        // 线程池
        ExecutorService boss = Executors.newCachedThreadPool();
        ExecutorService worker = Executors.newCachedThreadPool();

        // socket工厂
        bootstrap.setFactory(new NioClientSocketChannelFactory(boss, worker));

        // 管道工厂
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {

                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("decoder", new StringDecoder());
                pipeline.addLast("encoder", new StringEncoder());
                pipeline.addLast("hiHandler", new HiHandler());

                return pipeline;
            }
        });

        // 连接服务器
        ChannelFuture connect = bootstrap.connect(new InetSocketAddress("127.0.0.1", 10101));
        System.out.println("client start");

        // 客户端发送消息
        // 这里的 connect.getChannel() 和 messageReceived 方法 里的 ctx.getChannel() 是同一个 Channel
        Channel channel = connect.getChannel();
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("请输入");
            channel.write(sc.next());
        }
    }

}
