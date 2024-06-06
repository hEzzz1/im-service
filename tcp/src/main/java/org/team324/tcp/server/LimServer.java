package org.team324.tcp.server;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.team324.codec.MessageDecoder;
import org.team324.codec.MessageEncoder;
import org.team324.codec.config.BootstrapConfig;
import org.team324.tcp.handler.HeartBeatHandler;
import org.team324.tcp.handler.NettyServerHandler;


/**
 * @author crystalZ
 * @date 2024/6/2
 */
public class LimServer {

    private final static Logger logger = LoggerFactory.getLogger(LimServer.class.getName());

    BootstrapConfig.TcpConfig config;
    EventLoopGroup mainGroup;
    EventLoopGroup subGroup;
    ServerBootstrap server;

    public LimServer(BootstrapConfig.TcpConfig config) {
        this.config = config;
        mainGroup = new NioEventLoopGroup(config.getBossThreadSize());
        subGroup = new NioEventLoopGroup(config.getWorkThreadSize());
        server = new ServerBootstrap();
        server.group(mainGroup, subGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 10240)    //服务端可以连接的队列大小
                .option(ChannelOption.SO_REUSEADDR, true)   // 能够重复使用本地端口号和本机地址
                .childOption(ChannelOption.TCP_NODELAY, true)   //是否禁用Nagle算法 开启的话可以减少网络开销 但是影响消息实时性
                .childOption(ChannelOption.SO_KEEPALIVE, true)  // 保活开关
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
//                        ch.pipeline().addLast(new LoggingHandler());
                        ch.pipeline().addLast(new MessageDecoder());
                        ch.pipeline().addLast(new MessageEncoder());
//                        ch.pipeline().addLast(new IdleStateHandler(0,0,1));
                        ch.pipeline().addLast(new HeartBeatHandler(config.getHeartBeatTime()));
                        ch.pipeline().addLast(new NettyServerHandler(config.getBrokerId(), config.getLogicUrl()));
                    }
                });
    }

    public void start(){
        this.server.bind(this.config.getTcpPort());
    }
}
