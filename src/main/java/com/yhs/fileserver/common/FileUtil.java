package com.yhs.fileserver.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
	
	public static byte[] readFile(String fileName) throws Exception
	{
		File f = new File(fileName);
		if(!f.exists())
			throw new Exception("文件不存在");
		InputStream ins = new FileInputStream(f);
		BufferedInputStream in = new BufferedInputStream(ins);
		int lenght = (int)f.length();
		byte[] buffer = new byte[lenght];
		in.read(buffer,0, lenght);
		in.close();
		ins.close();
		f=null;
		return buffer;
	}
	
	public static void writeFile(String fileName,byte[] bytes) throws IOException
	{
		File f = new File(fileName);
		if(!f.exists())
		{
			f.createNewFile();
		}
		else
		{
			f.delete();
			f.createNewFile();
		}
		OutputStream os = new FileOutputStream(f);
		BufferedOutputStream out = new BufferedOutputStream(os);
		out.write(bytes);
		out.close();
		os.close();
		f=null;
	}
	
	public static void deleteFile(String name)
	{
		File f = new File(name);
		if(f.exists())
		{
			f.delete();
			f=null;
		}
	}
}
