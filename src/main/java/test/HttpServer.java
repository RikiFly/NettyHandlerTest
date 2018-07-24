package test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import test.handler.HttpHandler;

/**
 *
 * @author liqifei
 * @date 7/9/18
 */
public class HttpServer {

    public static void main(String[] args) throws InterruptedException {
        // 初始化ServerBootstrap实例，netty服务端应用开发的入口
        ServerBootstrap b = new ServerBootstrap();
        // 就是一个线程池，应该分为Boss和Worker，这里为了方便只用一个。
        // Boss负责接受Socket，并产生一个channel，之后从Worker线程池中找一个Worker来处理这个请求，Boss继续去处理其他socket请求
        // 默认线程数是 CPU × 2
        NioEventLoopGroup group = new NioEventLoopGroup();
        NioEventLoopGroup group2 = new NioEventLoopGroup();
        b.group(group, group2)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                // 用于解码request
                                .addLast("decoder", new HttpRequestDecoder())
                                // 用于编码response
                                .addLast("encoder", new HttpResponseEncoder())
                                // 消息聚合器，一次请求会分为多次会话，所有会话聚合在一起，才是一个完整的HTTP请求
                                // maxContentLength指聚合之后的长度不能超过512k
                                .addLast("aggregator", new HttpObjectAggregator(512 * 1024))
                                .addLast("handler", new HttpHandler());
                    }
                });
        b.bind(9999).sync();
    }
}
