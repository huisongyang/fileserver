package com.yhs.fileserver.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Stack;

import com.yhs.fileserver.common.ACTION;
import com.yhs.fileserver.common.Constant;
import com.yhs.fileserver.common.FileUtil;
import com.yhs.fileserver.common.MarshallingCodeCFactory;
import com.yhs.fileserver.core.FileScan;
import com.yhs.fileserver.pojo.Request;
import com.yhs.fileserver.pojo.Response;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;

/**
 * 
 * @author huisong
 * 
 */
public class ClientHandler extends ChannelHandlerAdapter {
	/**
	 * Creates a client-side handler.
	 */

	File file;
	OutputStream out;
	long fileSize;
	long writedSize = 0;

	public ClientHandler() {

	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		ctx.write(startReq());
		ctx.flush();
	}

	/**
	 * 更新请求
	 * 
	 * @return
	 */
	private Request startReq() {
		byte[] bytes = null;
		try {
			bytes = FileUtil
					.readFile(com.yhs.fileserver.common.Constant.clientMd5);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Request req = new Request();
		req.setActionCode(ACTION.ACTION_START);
		req.setData(bytes);
		req.setIp(Client.ip);
		return req;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		// consoleLog(msg.toString());
		if (msg instanceof ByteBuf) {
			ByteBuf buf = (ByteBuf) msg;
			if (out == null)
				throw new Exception("本地文件流没有初始化");
			int len = buf.readableBytes();

			// 如果文件大小是0KB，更新服务器则会直接响应个文件结尾符
			if (len == 19) {
				byte[] endBuf = new byte[19];
				buf.readBytes(endBuf, 0, 19);
				if (Constant.FILE_SEPARATOR.equals(new String(endBuf,CharsetUtil.UTF_8))) {
					closeFileAndOut(ctx);
					fireDownReq(ctx);
					return;
				}
			}

			buf.readBytes(out, len);
			writedSize += len;
			if (writedSize == fileSize) {
				closeFileAndOut(ctx);
				fireDownReq(ctx);
			}
		} else if (msg instanceof Response) {
			Response resp = (Response) msg;
			String resType = resp.getResType();
			if (resType.equals(Constant.TYPE_GET_DIFFERENT))// 差异列表
			{
				responseRead(ctx, resp);
				fireDownReq(ctx);
				deleteFiles();
			} else if (resType.equals(Constant.TYPE_GET_SIZE))// 文件大小
			{
				if (!resp.getResCode().equals(Constant.SuccessCode))
					throw new Exception(resp.getResMessage());
				fileSize = Long.parseLong(resp.getResMessage());
				String fn = resp.getReq().getReqMes();// 文件名
				initFile(fn);// 初始化文件流，准备发起下载请求
				ctx.pipeline().remove("marDecoder");
				beginDownFile(ctx, fn);
			}
		}
	}

	/**
	 * 开始下载文件
	 * 
	 * @param ctx
	 * @param fn
	 */
	private void beginDownFile(ChannelHandlerContext ctx, String fn) {
		consoleLog("下载文件:" + fn);
		Request req = new Request();
		req.setActionCode(ACTION.ACTION_DOWN);
		req.setReqMes(fn);
		req.setIp(Client.ip);
		ctx.writeAndFlush(req);
	}

	/**
	 * 发起文件下载请求<br>
	 * 按顺序发起下载请求，每次下载完一个文件，则重新请求下一个文件的下载<br>
	 * 每次发起都要保证保存及关闭当前文件
	 * 
	 * @see
	 * @param ctx
	 * @throws Exception
	 */
	private void fireDownReq(ChannelHandlerContext ctx) throws Exception {
		if (downList.empty()) {
			ctx.close();
			consoleLog("更新完成");
			return;
		}
		String fn = downList.pop();
		getFileSize(ctx, fn);// 获取文件大小
	}

	private void getFileSize(ChannelHandlerContext ctx, String fn)
			throws Exception {
		if (fn == null)
			throw new Exception("文件名不能为空");

		Request req = new Request();
		req.setActionCode(ACTION.ACTION_GET_FILE_SIZE);
		req.setIp(Client.ip);
		req.setReqMes(fn);// 文件名
		ctx.writeAndFlush(req);
	}

	private void initFile(String fn) {
		try {
			file = new File(FileScan.rootDirPath + fn);
			if (file.exists()) {
				file.delete();
				file.createNewFile();
			} else {
				File parent = file.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
			}
			out = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * 响应文件差异列表<br>
	 * 
	 * @param ctx
	 * @param msg
	 */
	@SuppressWarnings("unchecked")
	private void responseRead(ChannelHandlerContext ctx, Response resp) {

		if (resp.getResCode().equals(
				com.yhs.fileserver.common.Constant.SuccessCode)) {
			downList = new Stack<String>();
			consoleLog(resp.getResMessage());
			deleteFiles = (List<String>) resp.getDeleteFiles();
			newFiles = (List<String>) resp.getNewFiles();
			overrideFiles = (List<String>) resp.getOverrideFiles();
			for (String s : newFiles) {
				downList.push(s);
			}
			for (String i : overrideFiles) {
				downList.push(i);
			}
		}
	}

	/**
	 * 根据更新服务器返回的需删除文件列表进行文件的删除<br>
	 * 以及清空空文件夹
	 */
	private void deleteFiles() {
		if (!deleteFiles.isEmpty()) {
			Thread t = new Thread(new Runnable() {

				public void run() {
					for (String fn : deleteFiles) {
						File f = new File(FileScan.rootDirPath + fn);
						if (f.exists()) {
							f.delete();
							consoleLog("删除文件:" + f.getName());
						}
					}
					deleteEmptyDir(new File(FileScan.rootDirPath));
				}
			});

			t.start();
		}
	}

	/**
	 * 递归删除空文件夹
	 * 
	 * @param f
	 */
	private void deleteEmptyDir(File f) {
		for (File i : f.listFiles()) {
			if (i.isDirectory()) {
				deleteEmptyDir(i);
				if (i.isDirectory() && i.list().length == 0) {
					i.delete();
					consoleLog("清空空文件夹:" + i.getName());
				}
			}
		}
	}

	/**
	 * 关闭文件流<br>
	 * 每次从服务器获得一个完整文件后调用
	 * 
	 * @author huisong
	 */
	private void closeFileAndOut(ChannelHandlerContext ctx) {
		try {
			fileSize = 0;
			writedSize = 0;
			out.close();
			file = null;
			ctx.pipeline().addBefore("marEncoder", "marDecoder",
					MarshallingCodeCFactory.buildMarshallingDecoder());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void consoleLog(String msg) {
		System.out.println("[info]: " + msg);
	}

	private Stack<String> downList;
	private List<String> deleteFiles;
	private List<String> newFiles;
	private List<String> overrideFiles;

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
