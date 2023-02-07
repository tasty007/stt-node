package com.statter.listener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.CreateMode;

public class MyConnectionStateListener implements ConnectionStateListener {
	  private String zkPath;
	  private String regContent;
	  public MyConnectionStateListener(String zkPath, String regContent) {
	    this.zkPath = zkPath;
	    this.regContent = regContent;
	  }
	  @Override
	  public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
	    if (connectionState == ConnectionState.LOST) {
	      while (true) {
	        try {
	          if (curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
	            curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
	      .forPath(zkPath, regContent.getBytes("UTF-8"));
	            break;
	          }
	        } catch (InterruptedException e) {
	          
	          break;
	        } catch (Exception e) {
	          
	        }
	      }
	    }
	  }
	}
