package com.statter.client;

import java.math.BigInteger;

import javax.servlet.jsp.jstl.core.Config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.statter.common.HiteBassConstant;
import com.statter.core.common.InitUtils;
import com.statter.core.common.SpringContextUtils;
import com.statter.core.data.dic.model.Dictionary;
import com.statter.core.impl.UpdateServiceImpl;
import com.statter.gateway.PoolTaskInfo;
import com.statter.util.ZookeeperUtils;

public class MiningThread extends Thread {
	
	
	
	
	
	


	private static Log log = LogFactory.getLog(MiningThread.class);
	

	private ZKClient zkClient;
	
	public ZKClient getZkClient() {
		return zkClient;
	}

	public void setZkClient(ZKClient zkClient) {
		this.zkClient = zkClient;
	}
	@Override
	public void run() {
		ZookeeperUtils utils = new ZookeeperUtils();
        try {
			utils.connectZookeeper(HiteBassConstant.ZOOKEEPER_IP);
		} catch (Exception e1) {
			
			e1.printStackTrace();
		}
		while (true) {
			
			try {
				if(!zkClient.hasLeadership()){
					log.info("not leader");
                    MiningThread.sleep(5000);
                    
        			String index=utils.getNodeData2(HiteBassConstant.ZOOKER_BLOCKINDEX);
        			Dictionary dic = InitUtils.intiMainChainDic();
        			String maxBlockIndex = dic.getValue();
        			BigInteger mIndex=new BigInteger(maxBlockIndex);
        			BigInteger zIndex=new BigInteger(index);
    				UpdateServiceImpl update = new UpdateServiceImpl();

        			while(mIndex.compareTo(zIndex)<0 ) {
        					mIndex=mIndex.add(BigInteger.ONE);
        					update.updateWallet(mIndex.toString(), utils);
            				System.out.println(mIndex);
            				System.out.println(zIndex);
        				
        			}
                    continue;
				}
                    
                
			} catch (Exception e) {
				e.getMessage();
			}
			
		}
		
	}
}
