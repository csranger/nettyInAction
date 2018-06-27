package topic2NettyServer;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) {
        // 服务类
        ServerBootstrap  bootstrap = new ServerBootstrap();

        // 两个线程池: boss 和 worker 一个线程分配一个 selector
        // boss 的 selector 负责监听端口 worker 的 selector 负责 Channel 的读写任务
        ExecutorService boss = Executors.newCachedThreadPool();     // boss 中的selector负责监听端口的
        ExecutorService worker = Executors.newCachedThreadPool();     // worker 中的selector负责channel的读写任务

        // 为服务类ServerBootstrap设置一个niosocket工厂，传递两个线程池boss和worker
        bootstrap.setFactory(new NioServerSocketChannelFactory(boss, worker));

        // 设置管道工厂
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {

                // 获取一个管道，管道相当于装了许多过滤器
                ChannelPipeline pipeline = Channels.pipeline();


                pipeline.addLast("decoder", new StringDecoder());   // StringEncoder 实现ChannelUpStreamHandler接口
                pipeline.addLast("encoder", new StringEncoder());   // StringEncoder 实现ChannelDownStreamHandler接口
                // 注意此处  HelloHandler  必须在 decoder 和 encoder 后面
                pipeline.addLast("helloHandler", new HelloHandler());   // 处理消息接受和写 实现SimpleChannelhandler接口

                return pipeline;
            }
        });

        // 服务类绑定端口
        bootstrap.bind(new InetSocketAddress("127.0.0.1", 10101));
        System.out.println("服务器启动！");

        // netty 服务完成，如何接收消息？在 Handler 里的 messageReceived 方法里
        // netty 服务完成，如何接收消息？在 Handler 里的 messageReceived 方法里
        // 客户端连接进来会触发 connect 事件，执行 Handler 里的 channelConnected 方法
        // 客户端发送数据会执行 Handler 里的 messageReceived 方法
        // messageReceived 方法里抛异常就会触发 Handler 里的 exceptionCaught 方法 例如在 messageReceived 方法加入 int i = 1 / 0;
        // 连接已经建立，关闭通道时触发 Handler 里的 channelDisconnected 方法
        // Channel 关闭的时候触发 Handler 里的 channelClosed 方法
    }
}
