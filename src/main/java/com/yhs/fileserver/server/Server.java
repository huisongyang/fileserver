package com.yhs.fileserver.server;



import com.yhs.fileserver.common.MarshallingCodeCFactory;
import com.yhs.fileserver.core.FileScan;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 
 * @author huisong
 * 
 */
public class Server {

	public static ChannelHandler marshallingEncoderCache;

	public static void main(String[] args) throws Exception {
		int port = 9999;
		if (args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
			}
		}
		FileScan.getDefault().scanningAndWrite("md5.record");
		new Server().bind(port);
	}

	public void bind(int port) throws InterruptedException {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			marshallingEncoderCache = MarshallingCodeCFactory.buildMarshallingEncoder();

			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, 100)
			.handler(new LoggingHandler(LogLevel.ERROR))
			.childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch)
						throws Exception {

					ch.pipeline().addLast("marencoder",marshallingEncoderCache);
					ch.pipeline().addLast(
							MarshallingCodeCFactory
							.buildMarshallingDecoder());
					ch.pipeline().addLast("chunkedWriteHandler",new ChunkedWriteHandler());
					ch.pipeline().addLast("ServerHandler",new ServerHandler());

				}
			});

			ChannelFuture f = b.bind(port).sync();

			f.channel().closeFuture().sync();

		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
