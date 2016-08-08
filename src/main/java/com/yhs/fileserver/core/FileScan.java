package com.yhs.fileserver.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件扫描生成md5校验文件
 * 
 * @author huisong
 * 
 */
public class FileScan {

	public static String rootDirPath;
	MessageDigest md;
	int bufferSize = 1024 * 20;
	List<FileStructure> digestList;
	List<String> excludeList;
	List<String> excludeDirList;

	private static final FileScan instance = new FileScan();

	public static FileScan getDefault() {
		return instance;
	}

	FileScan() {
		rootDirPath = System.getProperty("root.dir.path");
		digestList = new ArrayList<FileScan.FileStructure>();
		excludeList = new ArrayList<String>();
		excludeDirList= new ArrayList<String>();
		getExcludeFiles();
		getExcludeDir();
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	private void getExcludeFiles() {
		String exclude = System.getProperty("root.dir.exclude");
		if (exclude != null && !"".equals(exclude)) {
			String[] files = exclude.split(";");
			for (String f : files) {
				excludeList.add(f);
			}
		}
	}
	
	private void getExcludeDir()
	{
		String excludeDir = System.getProperty("root.dir.excludeDir");
		if(excludeDir != null && !"".equals(excludeDir))
		{
			String[] dirs = excludeDir.split(";");
			for(String d : dirs)
			{
				excludeDirList.add(d);
			}
		}
	}

	static class FileStructure {
		String fileName;
		String md5;

		public FileStructure(String f, String m) {
			this.fileName = f;
			this.md5 = m;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getMd5() {
			return md5;
		}

		public void setMd5(String md5) {
			this.md5 = md5;
		}

	}

	public void scanningAndWrite(String fileName) throws Exception {
		digestListClear();
		if (rootDirPath == null||"".equals(rootDirPath)) {
			throw new Exception("root.dir.path不存在");
		}
		File rootDir = new File(rootDirPath);
		if(!rootDir.exists())
			throw new Exception("root.dir.path目录不存在");
		scanning(rootDir);
		writeFile(fileName);
	}

	private void digestListClear() {
		digestList.clear();
	}

	private void writeFile(String fileName) {

		File f;
		FileWriter writer;
		BufferedWriter bw;
		try {

			f = new File(fileName);
			if (f.exists())
				f.delete();
			f.createNewFile();

			writer = new FileWriter(f);
			bw = new BufferedWriter(writer);

			for (FileStructure item : digestList) {
				String line = item.fileName.replace("\\", "/") + "|" + item.md5;
				bw.append(line);
				bw.newLine();
			}
			bw.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

		}
	}

	public void scanning(File file) {
		for (File f : file.listFiles()) {
			if (f.isFile()) {

				if (!excludeList.contains(f.getName())) {
					try {
						String md5 = getMd5(f);
						String fileName = f.getPath().replace(rootDirPath, "");
						FileStructure fs = new FileStructure(fileName, md5);
						digestList.add(fs);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else
				{
					consoleLog("跳过文件校验:"+f.getName());
				}
			} else if (f.isDirectory()) {
				if(!excludeDirList.contains(f.getName()))
				{
					scanning(f);	
				}else
				{
					consoleLog("跳过目录校验:"+f.getName());
				}
			}
		}
	}

	public String getMd5(File f) throws IOException {

		FileInputStream in = new FileInputStream(f);
		DigestInputStream di = new DigestInputStream(in, md);

		byte[] buffer = new byte[bufferSize];
		while (di.read(buffer) > 0)
			;
		md = di.getMessageDigest();

		di.close();
		in.close();

		byte[] digest = md.digest();
		return byteArrayToHex(digest);
	}

	private static String byteArrayToHex(byte[] byteArray) {

		// 首先初始化一个字符数组，用来存放每个16进制字符
		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };
		// new一个字符数组，这个就是用来组成结果字符串的（解释一下：一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方））
		char[] resultCharArray = new char[byteArray.length * 2];
		// 遍历字节数组，通过位运算（位运算效率高），转换成字符放到字符数组中去
		int index = 0;
		for (byte b : byteArray) {
			resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
			resultCharArray[index++] = hexDigits[b & 0xf];
		}
		// 字符数组组合成字符串返回
		return new String(resultCharArray);

	}
	
	private void consoleLog(String msg)
	{
		System.out.println("[info]: "+msg);
	}
}
