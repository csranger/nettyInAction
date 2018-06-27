package topic2NettyServer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;

public class HelloHandler extends SimpleChannelHandler {


    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        // 接收数据
        System.out.println("messageReceived");
        // 以下message需要转换，麻烦，在管道里添加"过滤"即可
//        ChannelBuffer message = (ChannelBuffer) e.getMessage();
//        String s = new String(message.array());  // 字节数组 -> 字符串
//        System.out.println(s);
        String s = (String) e.getMessage();
        System.out.println(s);


        // 回写数据
        // 可从 ChannelHandlerContext ctx 获取会话的 SocketChannel

        // 以下回写的字符串需要转换，通接收可以在管道里添加"过滤"
//        ChannelBuffer copiedBuffer = ChannelBuffers.copiedBuffer("hi".getBytes());
//        ctx.getChannel().write(copiedBuffer);
        ctx.getChannel().write("hi");

        super.messageReceived(ctx, e);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        super.exceptionCaught(ctx, e);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        System.out.println("channelConnected");
        super.channelConnected(ctx, e);
    }

    // channelDisconnected 和 channelClosed 区分
    // 必须连接已经建立，关闭通道时才会触发
    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        System.out.println("channelDisconnected");
        super.channelDisconnected(ctx, e);
    }

    // Channel 关闭的时候触发，释放资源
    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        System.out.println("channelClosed");
        super.channelClosed(ctx, e);
    }
}
