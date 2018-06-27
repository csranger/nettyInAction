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

import java.util.Scanner;

public class Client {
    public static void main(String[] args) {

        // 服务类
        Bootstrap bootstrap = new Bootstrap();

        // 显然客户端不需要Boss的，不需要监听端口
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            // 设置线程池
            bootstrap.group(worker);

            //  设置socket工厂
            bootstrap.channel(NioSocketChannel.class);

            // 设置管道
            bootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new StringDecoder());
                    ch.pipeline().addLast(new StringEncoder());
                    ch.pipeline().addLast(new ClientHandler());
                }
            });

            // 连接服务端
            ChannelFuture connect = bootstrap.connect("127.0.0.1", 10101);
            System.out.println("client start");

            // 客户端发送消息
            Channel channel = connect.channel();
            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.print("请输入");
                channel.writeAndFlush(sc.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            worker.shutdownGracefully();
        }


    }
}
