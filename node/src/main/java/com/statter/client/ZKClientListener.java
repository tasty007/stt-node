package com.statter.client;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;

import com.statter.common.HiteBassConstant;
import com.statter.core.common.InitUtils;
import com.statter.core.common.SpringContextUtils;
import com.statter.core.data.ZKClientInfo;
import com.statter.core.data.block.model.Block;
import com.statter.core.data.block.service.impl.BlockServiceImpl;
import com.statter.core.data.dic.model.Dictionary;
import com.statter.core.impl.UpdateServiceImpl;
import com.statter.gateway.PoolTaskInfo;
import com.statter.impl.TaskManagerServiceImpl;
import com.statter.util.ZookeeperUtils;

public class ZKClientListener implements LeaderLatchListener{

	private static Log log = LogFactory.getLog(ZKClientListener.class);
	
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");

	@Override
	public void isLeader() {
		try {
			ZookeeperUtils utils = new ZookeeperUtils();
			utils.connectZookeeper(HiteBassConstant.ZOOKEEPER_IP);
			String index=utils.getNodeData(HiteBassConstant.ZOOKER_BLOCKINDEX);
			Dictionary dic = InitUtils.intiMainChainDic();
			String maxBlockIndex = dic.getValue();
			BigInteger mIndex=new BigInteger(maxBlockIndex);
			BigInteger zIndex=new BigInteger(index);

			while(mIndex.compareTo(zIndex)<0) {
				mIndex=mIndex.add(BigInteger.ONE);
				UpdateServiceImpl update = new UpdateServiceImpl();
				update.updateWallet(mIndex.toString(), utils);
				index=mIndex+"";
				
			}
			System.out.println(PoolTaskInfo.STATUS_MINING);
			
			
				
		} catch (UnsupportedEncodingException e) {
			
			e.printStackTrace();
		} catch (Exception e) {
			
			e.printStackTrace();
		}

		System.out.println(1111);
		ZKClientInfo.isLeader = true;
		
	}

	@Override
	public void notLeader() {
		ZKClientInfo.isLeader = false;
	}

}
