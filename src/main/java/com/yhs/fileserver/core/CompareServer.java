package com.yhs.fileserver.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 客户端需要删除的文件列表<br>
 * 客户端需要更新覆盖的文件列表<br> 
 * 客户端需要新增加的文件列表
 * @author huisong
 * 
 */
public class CompareServer {



	String clientMd5FileName;
	String serverMd5FileName;
	File clientMd5File;
	File serverMd5File;
	List<String> deleteList;
	List<String> overrideList;
	List<String> newList;

	Map<String, String> clientMd5Map;
	Map<String, String> serverMd5Map;

	public boolean isNeedUpdate() {

		if(deleteList.isEmpty()&&overrideList.isEmpty()&&newList.isEmpty())
		{
			return false;
		}
		return true;
	}

	public List<String> getDeleteList() {
		return deleteList;
	}

	public void setDeleteList(List<String> deleteList) {
		this.deleteList = deleteList;
	}

	public List<String> getOverrideList() {
		return overrideList;
	}

	public void setOverrideList(List<String> overrideList) {
		this.overrideList = overrideList;
	}

	public List<String> getNewList() {
		return newList;
	}

	public void setNewList(List<String> newList) {
		this.newList = newList;
	}



	public CompareServer(String clientMd5, String serverMd5) {
		this.clientMd5FileName = clientMd5;
		this.serverMd5FileName = serverMd5;

		deleteList = new ArrayList<String>();
		overrideList = new ArrayList<String>();
		newList = new ArrayList<String>();

		clientMd5Map = new HashMap<String, String>();
		serverMd5Map = new HashMap<String, String>();
	}

	private void loadMd5File() throws Exception {

		clientMd5Map.clear();
		serverMd5Map.clear();
		this.clientMd5File = new File(clientMd5FileName);
		this.serverMd5File = new File(serverMd5FileName);

		if(!clientMd5File.exists()||!serverMd5File.exists())
		{
			throw new Exception("md5校验文件缺失");
		}

		FileReader c = new FileReader(clientMd5File);
		FileReader s = new FileReader(serverMd5File);

		BufferedReader cr = new BufferedReader(c);
		BufferedReader sr = new BufferedReader(s);
		String line;
		while ((line = cr.readLine()) != null && !line.equals("")) {
			String[] items = line.split("\\|");

			clientMd5Map.put(items[0], items[1]);
		}
		cr.close();
		while ((line = sr.readLine()) != null && !line.equals("")) {
			String[] items = line.split("\\|");
			serverMd5Map.put(items[0], items[1]);
		}
		sr.close();
	}

	public void compare() throws Exception {
		loadMd5File();
		setdeleteAndOverrideList();
		setNewList();
	}

	private void setdeleteAndOverrideList() {
		Set<String> k = serverMd5Map.keySet();
		for(String i : clientMd5Map.keySet())
		{
			if(!k.contains(i))
			{
				deleteList.add(i);
			}
			else
			{
				String md51 = serverMd5Map.get(i);
				String md52 = clientMd5Map.get(i);
				if(!md51.equals(md52))
				{
					overrideList.add(i);
				}
			}
		}
	}

	private void setNewList()
	{
		for(String i : serverMd5Map.keySet())
		{
			if(!clientMd5Map.containsKey(i))
			{
				newList.add(i);
			}
		}
	}
}
