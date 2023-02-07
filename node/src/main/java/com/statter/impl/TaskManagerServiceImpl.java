package com.statter.impl;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.statter.core.common.DataUtils;
import com.statter.core.data.block.model.Block;
import com.statter.core.data.p2p.trade.TradeBodyPool;
import com.statter.core.encrypt.EncryptUtil;
import com.statter.gateway.BlockPoolResultReqVo;


@Component
public class TaskManagerServiceImpl {
	
	private String workLoad;
	
	private BigInteger blockIndex;
	
	private String blockHash;
	
	private Block block;
	
	private String status;
	
	private TradeBodyPool baseTradeBodyPool;
	
	private Map<String, TradeBodyPool> tradeBodyPoolMap;
	
	
	public void validateWorkLoad(BlockPoolResultReqVo blockPoolResultReqVo) throws Exception {
		BigInteger blockIndex = blockPoolResultReqVo.getBlockIndex();
		String randomNumber = blockPoolResultReqVo.getRandomNumber();
		String walletAddress = blockPoolResultReqVo.getWalletAddress();
		TradeBodyPool tbp = tradeBodyPoolMap.get(walletAddress);
		Block newblock = new Block();
		BeanUtils.copyProperties(newblock, block);
		newblock.setRandomNumber(randomNumber);
		newblock.setCreateTime(blockPoolResultReqVo.getCreateTime());
		block.setEndHash(DataUtils.getEndHash(getBlockHash(), newblock.getPath(), newblock.getCreateTime(), newblock.getRandomNumber()));
		newblock.setEndHash(block.getEndHash());
		String outStr = blockHash + newblock.genString();
		String outHash = EncryptUtil.encryptSHA256(outStr);
		if(!outHash.startsWith(workLoad)) {
			System.out.println(outStr);
			System.out.println(newblock.genString());
			throw new Exception("false");
		}
		System.out.println("-------------------------------------");
		System.out.println(outHash);
		System.out.println(outStr);
		System.out.println(newblock.genString());
		block.setRandomNumber(newblock.getRandomNumber());
		block.setCreateTime(newblock.getCreateTime());
	}
	
	public TaskManagerServiceImpl() {
		blockIndex = BigInteger.ZERO;
		tradeBodyPoolMap = new HashMap<String, TradeBodyPool>();
	}
	
	public BigInteger getBlockIndex() {
		return blockIndex;
	}
	public void setBlockIndex(BigInteger blockIndex) {
		this.blockIndex = blockIndex;
	}
	public TradeBodyPool getBaseTradeBodyPool() {
		return baseTradeBodyPool;
	}
	public void setBaseTradeBodyPool(TradeBodyPool baseTradeBodyPool) {
		this.baseTradeBodyPool = baseTradeBodyPool;
	}
	public Map<String, TradeBodyPool> getTradeBodyPoolMap() {
		return tradeBodyPoolMap;
	}
	public void setTradeBodyPoolMap(Map<String, TradeBodyPool> tradeBodyPoolMap) {
		this.tradeBodyPoolMap = tradeBodyPoolMap;
	}
	public String getStatus() {
		if(StringUtils.isBlank(status)) {
			return "1";
		}
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public String getBlockHash() {
		return blockHash;
	}

	public void setBlockHash(String blockHash) {
		this.blockHash = blockHash;
	}

	public Block getBlock() {
		return block;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

	public String getWorkLoad() {
		return workLoad;
	}

	public void setWorkLoad(String workLoad) {
		this.workLoad = workLoad;
	}
	
}
