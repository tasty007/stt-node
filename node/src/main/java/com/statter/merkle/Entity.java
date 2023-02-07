package com.statter.merkle;

import java.util.List;

public class Entity {

	
	private  List<String> txList;
	
	private String hash;

	public List<String> getTxList() {
		return txList;
	}

	public void setTxList(List<String> txList) {
		this.txList = txList;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}
	
	
}
