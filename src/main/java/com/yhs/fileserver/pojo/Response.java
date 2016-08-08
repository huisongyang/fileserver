package com.yhs.fileserver.pojo;

import java.io.Serializable;

/**
 * 
 * @author huisong
 *
 */
public class Response implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Request req;
	
	String resMessage;
	
	String resType;
	
	String resCode;
	
	public String getResType() {
		return resType;
	}

	public void setResType(String resType) {
		this.resType = resType;
	}

	Object deleteFiles;
	
	Object newFiles;
	
	Object overrideFiles;

	public String getResCode() {
		return resCode;
	}

	public void setResCode(String resCode) {
		this.resCode = resCode;
	}

	public Object getDeleteFiles() {
		return deleteFiles;
	}

	public void setDeleteFiles(Object deleteFiles) {
		this.deleteFiles = deleteFiles;
	}

	public Object getNewFiles() {
		return newFiles;
	}

	public void setNewFiles(Object newFiles) {
		this.newFiles = newFiles;
	}

	public Object getOverrideFiles() {
		return overrideFiles;
	}

	public void setOverrideFiles(Object overrideFiles) {
		this.overrideFiles = overrideFiles;
	}

	public byte[] data;

	public Request getReq() {
		return req;
	}

	public void setReq(Request req) {
		this.req = req;
	}

	public String getResMessage() {
		return resMessage;
	}

	public void setResMessage(String resMessage) {
		this.resMessage = resMessage;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
}
