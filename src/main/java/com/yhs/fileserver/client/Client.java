package com.yhs.fileserver.client;

import java.net.InetAddress;

import com.yhs.fileserver.common.MarshallingCodeCFactory;
import com.yhs.fileserver.core.FileScan;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 
 * @author huisong
 *
 */
public class Client {
	
	public static String ip ;
	
	public void connect(int port, String host) throws Exception {
		// 配置客户端NIO线程组
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch)
								throws Exception {
							ch.pipeline().addLast("marDecoder",MarshallingCodeCFactory.buildMarshallingDecoder());
							ch.pipeline().addLast("marEncoder",
									MarshallingCodeCFactory
											.buildMarshallingEncoder());
							ch.pipeline().addLast(new ClientHandler());
						}
					});
			// 发起异步连接操作
			ChannelFuture f = b.connect(host, port).sync();

			// 等待客户端链路关闭
			f.channel().closeFuture().sync();
		} finally {
			// 优雅退出，释放NIO线程组
			group.shutdownGracefully();
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int port = 9999;
		String host = "127.0.0.1";
		if (args != null && args.length > 0) {
			try {
				host = args[0];
				port = Integer.valueOf(args[1]);
			} catch (NumberFormatException e) {
				// 采用默认值
			}
		}
		FileScan.getDefault().scanningAndWrite("clientMd5.record");
		ip = InetAddress.getLocalHost().toString();
		new Client().connect(port, host);
	}
}
