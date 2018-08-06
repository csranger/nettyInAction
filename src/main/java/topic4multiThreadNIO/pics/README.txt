使用多线程 NIO 实现多人会话

1、我们如何提高NIO的工作效率

2、一个NIO是不是只能有一个selector？

不是，一个系统可以有多个selector

3、selector是不是只能注册一个ServerSocketChannel？

不是，可以注册多个



netty是基于NIO实现的，见图NettyIO.png，这里使用NIO实现简单的Netty，即多线程NIO系统

