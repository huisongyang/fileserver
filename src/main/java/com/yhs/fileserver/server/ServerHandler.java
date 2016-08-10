package com.yhs.fileserver.server;

import java.io.RandomAccessFile;

import com.yhs.fileserver.common.Constant;
import com.yhs.fileserver.common.FileUtil;
import com.yhs.fileserver.core.CompareServer;
import com.yhs.fileserver.core.FileScan;
import com.yhs.fileserver.pojo.Request;
import com.yhs.fileserver.pojo.Response;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.util.CharsetUtil;

/**
 * 
 * @author huisong
 * 
 */
public class ServerHandler extends ChannelHandlerAdapter {

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		super.channelReadComplete(ctx);
	}
	
	/**
	 * 写入文件结尾符
	 * @param channel
	 */
	private void writeContenEnd(Channel channel) {
		ByteBuf buffer = channel.alloc().buffer(6);
		byte[] buf = new String(Constant.FILE_SEPARATOR).getBytes(CharsetUtil.UTF_8);
		buffer.writeBytes(buf);
		channel.pipeline().writeAndFlush(buffer);
		channel.pipeline().addBefore("chunkedWriteHandler", "marencoder", Server.marshallingEncoderCache);
	}
	
	

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		Request req = (Request) msg;
		consoleLog("请求类型 ["+req.getActionCode() + "] ip [" + req.getIp()+"]");
		int code = req.getActionCode();

		switch (code) {
		//差异列表
		case 0x001:
			byte[] bytes = req.getData();
			String md5Name = Constant.clientMd5 + ctx.channel().id();
			FileUtil.writeFile(md5Name, bytes);
			CompareServer compare = new CompareServer(md5Name,Constant.serverMd5);
			compare.compare();
			FileUtil.deleteFile(md5Name);
			Response res = new Response();
			res.setDeleteFiles(compare.getDeleteList());
			res.setNewFiles(compare.getNewList());
			res.setOverrideFiles(compare.getOverrideList());
			res.setResCode(Constant.SuccessCode);
			res.setResMessage("成功获取文件差异列表");
			res.setResType(Constant.TYPE_GET_DIFFERENT);
			ctx.writeAndFlush(res);
			break;
		
		//文件大小
		case 0x006:
			String name = FileScan.rootDirPath + req.getReqMes();
			RandomAccessFile raf0 = new RandomAccessFile(name,"r");
			long size = raf0.length();
			raf0.close();
			Response res1 = new Response();
			res1.setReq(req);
			res1.setResCode(Constant.SuccessCode);
			res1.setResType(Constant.TYPE_GET_SIZE);
			res1.setResMessage(String.valueOf(size));
			ctx.writeAndFlush(res1);
			break;
			
		//文件下载
		case 0x002:
			
			ctx.pipeline().remove("marencoder");
			String fileName = FileScan.rootDirPath + req.getReqMes();
			final RandomAccessFile raf = new RandomAccessFile(fileName,"r");
			
			long count = raf.length();
			if(count == 0)
			{
				consoleLog("文件长度为0-->"+fileName);
				writeContenEnd(ctx.channel());
				return;
			}
			//zero-copy
			ChannelFuture future = ctx.writeAndFlush(new DefaultFileRegion(raf.getChannel(), 0, raf.length()),
					ctx.newProgressivePromise());

			future.addListener(new ChannelProgressiveFutureListener() {

				public void operationComplete(ChannelProgressiveFuture future)
						throws Exception {
					future.channel().pipeline().addBefore("chunkedWriteHandler", "marencoder", Server.marshallingEncoderCache);
					raf.close();
				}
				
				public void operationProgressed(ChannelProgressiveFuture future,
						long progress, long total) throws Exception {
					
				}
			});
			break;
		default:
			break;
		}
	}

	private void consoleLog(String msg)
	{
		System.out.println("[info]:"+msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();// 发生异常，关闭链路
	}
}
