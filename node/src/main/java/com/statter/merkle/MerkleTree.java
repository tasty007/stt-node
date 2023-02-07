package com.statter.merkle;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class MerkleTree {
	
	private  List<String> txList;
	
	private String root;
	

	private int n;

	public void merkle_tree(List<String> txList) {
		 
        List<String> tempTxList = new ArrayList<String>();
 
        for (int i = 0; i < txList.size(); i++) {
            tempTxList.add(txList.get(i));
        }
 
        List<String> newTxList = getNewTxList(tempTxList);
 
        
        while (newTxList.size() != 1) {
            newTxList = getNewTxList(newTxList);
        }
 
        this.root = newTxList.get(0);
    }

	public TreeBlock getProot( Entity tree){
		TreeBlock tb=new TreeBlock();
		tree = getNewTxList(tree,tb);
		while (tree.getTxList().size() != 1) {
			tree= getNewTxList(tree,tb);
        }
		
		return tb;
		
	}
	private Entity getNewTxList(Entity tree,TreeBlock tb) {
		 
        List<String> newTxList = new ArrayList<String>();
        int index = 0;
        while (index < tree.getTxList().size()) {
           
            String left = tree.getTxList().get(index);
            index++;
           
            String right = "";
            if (index != tree.getTxList().size()) {
                right = tree.getTxList().get(index);
            }
            
            String sha2HexValue = getSHA2HexValue(left + right);
            if(left.equals(tree.getHash())||right.equals(tree.getHash())) {
            	
            	Tree t = new Tree();
            	if(!left.equals(tree.getHash())) {
            		t.setLeft(left);
            	}
            	if(!right.equals(tree.getHash())){
            		t.setRight(right);
            	}
            	
            	tree.setHash(sha2HexValue);
            	System.out.println("sha2HexValue:  " +sha2HexValue);
            	t.setNum(tb.getNum());
            	tb.setNum(tb.getNum()+1);
            	tb.getTs().add(t);
            }
           
            newTxList.add(sha2HexValue);
            index++;
 
        }
        tree.setTxList(newTxList);
        return tree;
    }
	 private List<String> getNewTxList(List<String> tempTxList) {
		 
	        List<String> newTxList = new ArrayList<String>();
	        int index = 0;
	        String str= "";
	        while (index < tempTxList.size()) {
	            
	            String left = tempTxList.get(index);
	            index++;
	            
	            String right = "";
	            if (index != tempTxList.size()) {
	                right = tempTxList.get(index);
	            }
	            
	            String sha2HexValue = getSHA2HexValue(left + right);
	            
	           
	            newTxList.add(sha2HexValue);
	            index++;
	            n++;
	        }
	 
	        return newTxList;
	    }
	 public String getSHA2HexValue(String str) {
	        byte[] cipher_byte;
	        try{
	            MessageDigest md = MessageDigest.getInstance("SHA-256");
	            md.update(str.getBytes());
	            cipher_byte = md.digest();
	            StringBuilder sb = new StringBuilder(2 * cipher_byte.length);
	            for(byte b: cipher_byte) {
	                sb.append(String.format("%02x", b&0xff) );
	            }
	            return sb.toString();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	 
	        return "";
	    }

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public List<String> getTxList() {
		return txList;
	}

	public void setTxList(List<String> txList) {
		this.txList = txList;
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

}
