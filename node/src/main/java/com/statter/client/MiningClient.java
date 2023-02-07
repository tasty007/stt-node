package com.statter.client;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.stereotype.Component;

import com.statter.common.HiteBassConstant;
import com.statter.core.common.IpUtils;


@Component
public class MiningClient {
	private static Log log = LogFactory.getLog(MiningClient.class);

	
	
	public  void Mining() {
		
		CuratorFramework client =
                CuratorFrameworkFactory.builder()
                        .connectString(HiteBassConstant.ZOOKEEPER_IP)
                        .retryPolicy(new ExponentialBackoffRetry(5000, 3))
                        .connectionTimeoutMs(5000)
                        .build();
		IpUtils ipUtils=new IpUtils();
		String ip=ipUtils.getPublicIP();
		LeaderLatch leaderLatch = new LeaderLatch(client, HiteBassConstant.LEADERLATCH, ip, LeaderLatch.CloseMode.NOTIFY_LEADER);
        ZKClientListener zkClientListener = new ZKClientListener();
        leaderLatch.addListener(zkClientListener);
        
        ZKClient zkClient = new ZKClient(leaderLatch,client);
        try {
        	zkClient.startZKClient();
		} catch (Exception e) {
			return;
		}
		
        
        
        MiningThread mThread=new MiningThread();
        mThread.setZkClient(zkClient);

        mThread.start();
        
        
	}
	public static String getNativeIP(){
	    try {
	        InetAddress candidateAddress = null; 
	        
	        for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
	            NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
	            
	            for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
	                InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
	                if (!inetAddr.isLoopbackAddress()) {
	                    if (inetAddr.isSiteLocalAddress()) {
	                        
	                        return inetAddr.getHostAddress();
	                    } else if (candidateAddress == null) {
	                        
	                        candidateAddress = inetAddr;
	                    }
	                }
	            }
	        }
	        if (candidateAddress != null) {
	            return candidateAddress.getHostAddress();
	        }
	        
	        InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
	        return jdkSuppliedAddress.getHostAddress();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return "";
	}
	

}
