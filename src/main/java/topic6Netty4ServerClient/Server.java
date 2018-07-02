package topic6Netty4ServerClient;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
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

            // netty3设置如下
//            bootstrap.setOption("backlog", 2048);
//            bootstrap.setOption("keepAlive", true);
//            bootstrap.setOption("tcpNoDelay", true);
            // 设置参数 TCP参数，还有很多其它参数
            bootstrap.option(ChannelOption.SO_BACKLOG, 2048);  // 连接缓冲池的大小，ServerSocketChannel的设置，accept连接会放入一个2048大小的"队列"
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);// SocketChannel的设置，维持连接的活跃，清除一些死连接
            bootstrap.option(ChannelOption.TCP_NODELAY, true);  // SocketChannel的设置，关闭延迟发送


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
