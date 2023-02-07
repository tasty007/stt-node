package com.statter.core.impl;

import java.awt.Canvas;
import java.awt.Label;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.statter.common.HiteBassConstant;
import com.statter.core.common.BtcConstant;
import com.statter.core.common.DataUtils;
import com.statter.core.common.IpUtils;
import com.statter.core.common.block.BlockBaseHandler;
import com.statter.core.common.block.BlockBaseUtils;
import com.statter.core.common.exception.ErrorInfo;
import com.statter.core.common.exception.TokException;
import com.statter.core.common.exception.UpdateException;
import com.statter.core.common.http.client.HttpClientHelper;
import com.statter.core.data.IpAddress.model.IpAddress;
import com.statter.core.data.block.model.Block;
import com.statter.core.data.block.model.BlockAddressList;
import com.statter.core.data.block.model.BlockDownLoad;
import com.statter.core.data.block.service.impl.BlockServiceImpl;
import com.statter.core.data.dic.dicService.impl.DicServiceImpl;
import com.statter.core.data.dic.model.Dictionary;
import com.statter.core.data.friends.model.Friends;
import com.statter.core.data.friends.service.impl.FriendServiceImpl;
import com.statter.core.data.p2p.NoticeParams;
import com.statter.core.data.p2p.trade.TradeBody;
import com.statter.core.data.p2p.trade.TradeBodyPool;
import com.statter.core.data.p2p.trade.senior.trade.ContractToContractAddressTradebody;
import com.statter.core.data.p2p.trade.senior.trade.ContractWithdrawTradebody;
import com.statter.core.data.p2p.trade.senior.trade.CreateContractTradeBody;
import com.statter.core.data.p2p.trade.senior.trade.TokToContractAddressTradebody;
import com.statter.core.data.p2p.trade.timebank.LoanByFBDTTradeBody;
import com.statter.core.data.p2p.trade.timebank.LoanByTOKTradeBody;
import com.statter.core.data.p2p.trade.timebank.RepaymentTradeBody;
import com.statter.core.data.p2p.trade.timebank.StorageTradeBody;
import com.statter.core.data.p2p.trade.timebank.TradeStatusTradeBody;
import com.statter.core.data.pending.service.impl.PendingServiceImpl;
import com.statter.core.thread.MiningThread;
import com.statter.core.util.HTTPUtils;
import com.statter.merkle.MerkleTree;
import com.statter.util.ZookeeperUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;



public class UpdateServiceImpl extends BlockBaseHandler{
	private static final Logger logger = Logger.getLogger(UpdateServiceImpl.class);

	
	public static BigInteger currentUpdateBlockIndex = BigInteger.ONE.negate();
	public static BigInteger currentMaxBlockIndex = BigInteger.ZERO;
	public static Block currentPreUpdateBlock = null;
	
	public void  updateWallet(String blockIndex,ZookeeperUtils util) throws  TokException, Exception {
		 String ip=util.getLeader2();
		 BlockDownLoad bdl = null;
		 NoticeParams np = new NoticeParams(blockIndex, ip,"");
			bdl = HttpClientHelper.downLoadBlock(ip, HiteBassConstant.HTTP_PORT, np);
			if(bdl == null){
				throw new Exception("block false");
			}
			boolean isCheck=true;
			Integer maxIndex=Integer.valueOf(blockIndex);
			if(currentPreUpdateBlock==null) {
				if( maxIndex>0) {
					Integer index=maxIndex-1;
					Block block = BlockServiceImpl.queryBlockByBlockIndexOnMingChain(index.toString());
					currentPreUpdateBlock=block;
					
				}else {
					isCheck=false;
				}
				
			}
			
			validateBlockHash(bdl);
			
			if(currentUpdateBlockIndex.compareTo(BigInteger.ZERO) != 0) {
				if(!currentPreUpdateBlock.getEndHash().equals(bdl.getBlock().getHeadHash())) {
					
					
					throw new TokException(ErrorInfo.BLOCK_CHAIN_BREAK_CODE, ErrorInfo.BLOCK_CHAIN_BREAK_CODE_MSG);
				}
			}
			saveBlock(bdl.getBlock(), currentUpdateBlockIndex, bdl, true);
	}
	
	
	public void saveBlock(Block block, BigInteger index, BlockDownLoad bdl, boolean isDown) throws TokException{
		TradeBodyPool tbp = BlockBaseUtils.genTbp(bdl);
		
		Map<String, BlockAddressList> balAllMap =  BlockBaseUtils.genBalAllMap(tbp, bdl.getBlock());
		BlockServiceImpl.checkBlockTable(bdl.getBlock().getBlockIndex());
		BigInteger blockIndex = new BigInteger(bdl.getBlock().getBlockIndex());
		if(isDown) {
			
			bdl.getBlock().setCounter(0);
			BlockServiceImpl.save(bdl.getBlock());
			updateBalMap(balAllMap);
			BlockServiceImpl.saveBlockFile(bdl);
			if(StringUtils.isNotBlank(bdl.getContractBlockFileStr())) {
				BlockServiceImpl.saveContractBlockFile(bdl);
			}
			BlockServiceImpl.saveBlockInFile(bdl.getBlock(), balAllMap);
			BlockBaseHandler.updateAllBlockCounter2(tbp, bdl.getBlock());
		}else {
			
			BlockServiceImpl.checkBlockTable(bdl.getBlock().getBlockIndex());
			
			
			
			updateBalMap(balAllMap);
			
			BlockServiceImpl.saveBlockInFile(bdl.getBlock(), balAllMap);
			
			
			if(StringUtils.isNotBlank(bdl.getContractBlockFileStr())) {
				BlockServiceImpl.saveContractBlockFile(bdl);
			}
			
			BlockBaseHandler.updateBlockCounter3(tbp, bdl.getBlock(), balAllMap);
		}
		
			updateBlockAllAddressList(balAllMap, 1, bdl.getBlock().getBlockIndex());
		
		DicServiceImpl.updateDicBlockIndex(index.toString());
		DicServiceImpl.updateDicMainBockIndex(index.toString());
		deletePending(tbp);
	}
	
	public void registerIp() {
		
		boolean b = IpUtils.isInternalIp();
		
		try {
			URL friendUrl = new URL(BtcConstant.GET_ROUND_ID);
			HTTPUtils  uti=new HTTPUtils();
			
			String resp=  HTTPUtils.post(friendUrl,"");
			if(resp!=null || !resp.equals("")) {
			JSONObject obj = JSONObject.fromObject(resp);
			String code=(String) obj.get("code");
    		if(!BtcConstant.CLIENT_TYPE.equals("Intranet")) {
		
         if(code.equals("0")) {
       	  JSONObject map= (JSONObject) obj.get("map");
             JSONArray array = map.getJSONArray("IpAddress");
             List<IpAddress> list= new ArrayList<IpAddress>();
             for(int i=0;i<array.size();i++) {
           	  IpAddress ipAddres = new IpAddress();
           	  JSONObject jo = array.getJSONObject(i);
           	  String ip=(String) jo.get("ip");
           	  ipAddres.setAddress(ip);
           	list.add(ipAddres);
             }
           	 
         if(list!=null && list.size()>0) {
        	 saveList(list,0,"0");
 		}
         List<IpAddress> listTwo= new ArrayList<IpAddress>();
         JSONArray arrayTwo = map.getJSONArray("newIpAddress");
         for(int i=0;i<arrayTwo.size();i++) {
       	  IpAddress ipAddres = new IpAddress();
       	  JSONObject jo = arrayTwo.getJSONObject(i);
       	  String ip=(String) jo.get("ip");
       	  ipAddres.setAddress(ip);
       	  listTwo.add(ipAddres);
         }
         if(listTwo!=null && listTwo.size()>0) {
        	 saveList(listTwo,1,"0");
         }
       }
         
			URL url = new URL(BtcConstant.REGISTER_IP);
			HTTPUtils  registerIp=new HTTPUtils();
			HTTPUtils.post(url,"");		
		}} 
	}catch (MalformedURLException e) {
		
		
	}
	
		
	}
	
	public static void saveList(List<IpAddress> list,int type,String friendType) {
		for(IpAddress ip : list) {
				try {
					String local=InetAddress.getLocalHost().getHostAddress();
					Friends check=FriendServiceImpl.queryFriendsByIp(ip.getAddress());
					if(local.equals(ip.getAddress())) {
						continue;
					}else if(check!=null){
						continue;
					}else {
						String add=ip.getAddress();
						Friends friends = new Friends();
						friends.setIp(add);
						friends.setAlive(0);
						friends.setFriendliness(0);
						friends.setPort(String.valueOf(BtcConstant.HTTP_PORT));
						friends.setSuperNode(friendType);
						friends.setCreateTime(DataUtils.getCurrentTime());
						friends.setLastTime(DataUtils.getCurrentTime());
						FriendServiceImpl.save(friends);
						if(type==0) {
							Thread thread = new Thread(){
								   @Override
								public void run(){
									   try {
										HttpClientHelper.registerIp(add, BtcConstant.HTTP_PORT);
									} catch (Exception e) {
										
										e.printStackTrace();
									}
								   }
								};
								thread.start();
						}
					}
				} catch (Exception e) {
					
				}
			}
	}

	
	public void saveOrDownload(Block block, Label lbl, Canvas canvas) throws TokException, Exception {
		List<Friends> ips = FriendServiceImpl.queryAllFriendsSuperNode();
		BlockDownLoad bdl = null;
		for(Friends f : ips) {
			
			
			NoticeParams noticeParams = new NoticeParams();
			noticeParams.setBn(currentUpdateBlockIndex.toString());
			noticeParams.setIp(f.getIp());
			
			
			bdl = HttpClientHelper.downLoadBlock(null, BtcConstant.HTTP_PORT, noticeParams);
			if(bdl == null) {
     		   FriendServiceImpl.updateAlive(f.getIp(),BtcConstant.ALIVE_ADD);
     		   continue;
			}
			
			
			try {
				
				validateBlockHash(bdl);
				
				if(currentUpdateBlockIndex.compareTo(BigInteger.ZERO) != 0) {
					if(!currentPreUpdateBlock.getEndHash().equals(bdl.getBlock().getHeadHash())) {
						
	            		   FriendServiceImpl.updateAlive(f.getIp(),BtcConstant.ALIVE_ADD);
						throw new TokException(ErrorInfo.BLOCK_CHAIN_BREAK_CODE, ErrorInfo.BLOCK_CHAIN_BREAK_CODE_MSG);
					}
					break;
				}else {
					break;
				}
			} catch (Exception e) {
				logger.warn(noticeParams.getIp() + ErrorInfo.UPDATE_BLOCK_NOT_FOUND_CODE_MSG + currentUpdateBlockIndex, e);
				continue;
			}
		}
		
		if(bdl == null) {
			throw new UpdateException(ErrorInfo.UPDATE_BLOCK_ALL_NOT_FOUND_CODE, ErrorInfo.UPDATE_BLOCK_ALL_NOT_FOUND_CODE_MSG + currentUpdateBlockIndex);
		}
		
		saveBlock(block, currentUpdateBlockIndex, bdl, true);
		
	
		
	}

	
	public void deletePending(TradeBodyPool tbp) {
		
		List<String> tradeNos = new ArrayList<String>();
		Map<String, TradeBody> tbMap = tbp.getTbMap();
		Map<String, CreateContractTradeBody> cctbMap =tbp.getCctbMap();
		Map<String, ContractToContractAddressTradebody> ctcatbMap = tbp.getCtcatbMap();
		Map<String, ContractWithdrawTradebody> cwtbMap = tbp.getCwtbMap();
		Map<String, TokToContractAddressTradebody> ttcatbMap = tbp.getTtcatbMap();
		for(Entry<String, TradeBody> entry : tbMap.entrySet()) {
			tradeNos.add(entry.getValue().getTradeNo());
		}
		for(Entry<String, CreateContractTradeBody> entry: cctbMap.entrySet()) {
			tradeNos.add(entry.getValue().getTradeNo());
		}
		for(Entry<String, ContractToContractAddressTradebody> entry: ctcatbMap.entrySet()) {
			tradeNos.add(entry.getValue().getTradeNo());
		}
		for(Entry<String, ContractWithdrawTradebody> entry: cwtbMap.entrySet()) {
			tradeNos.add(entry.getValue().getTradeNo());
		}
		for(Entry<String, TokToContractAddressTradebody> entry: ttcatbMap.entrySet()) {
			tradeNos.add(entry.getValue().getTradeNo());
		}
		Map<String, LoanByFBDTTradeBody> lbFbdttbMap = tbp.getTimebankTbPool().getLbFbdttbMap();
		Map<String, LoanByTOKTradeBody> lbToktbMap = tbp.getTimebankTbPool().getLbToktbMap();
		Map<String, RepaymentTradeBody> rtbMap = tbp.getTimebankTbPool().getRtbMap();
		Map<String, StorageTradeBody> stbMap = tbp.getTimebankTbPool().getStbMap();
		Map<String, TradeStatusTradeBody> tstbMap = tbp.getTimebankTbPool().getTstbMap();
		for(Entry<String, LoanByFBDTTradeBody> entry: lbFbdttbMap.entrySet()) {
			tradeNos.add(entry.getValue().getTradeNo());
		}
		for(Entry<String, LoanByTOKTradeBody> entry: lbToktbMap.entrySet()) {
			tradeNos.add(entry.getValue().getTradeNo());
		}
		for(Entry<String, RepaymentTradeBody> entry: rtbMap.entrySet()) {
			tradeNos.add(entry.getValue().getTradeNo());
		}
		for(Entry<String, StorageTradeBody> entry: stbMap.entrySet()) {
			tradeNos.add(entry.getValue().getTradeNo());
		}
		for(Entry<String, TradeStatusTradeBody> entry: tstbMap.entrySet()) {
			tradeNos.add(entry.getValue().getTradeNo());
		}
		PendingServiceImpl.deletePendings(tradeNos);
	}
	
}
