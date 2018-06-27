package topic6Netty4ServerClient;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * 使用netty4实现 Server
 * netty4引用的类来自 io.netty 而netty3来自 org.jboss.netty
 */
public class Server {

    public static void main(String[] args) {

        // 服务类
        ServerBootstrap bootstrap = new ServerBootstrap();

        // boss worker 线程池 -> 事件循环组
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {

            // 设置线程池
            bootstrap.group(boss, worker);

            // 设置socket 工厂
            bootstrap.channel(NioServerSocketChannel.class);

            // 设置管道工厂 ->
            bootstrap.childHandler(new ChannelInitializer<Channel>() {
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new StringDecoder());
                    ch.pipeline().addLast(new StringEncoder());
                    ch.pipeline().addLast(new ServerHandler());
                }
            });

            // 绑定端口
            ChannelFuture future = bootstrap.bind(10101);
            System.out.println("服务器启动!");

            // 等待服务端关闭  future.channel()指的是ServerSocketChannel
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }

}
