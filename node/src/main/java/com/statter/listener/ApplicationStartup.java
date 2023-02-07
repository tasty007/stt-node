package com.statter.listener;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.statter.cache.TaskManagerCache;
import com.statter.client.MiningClient;
import com.statter.common.HiteBassConstant;
import com.statter.core.common.InitUtils;
import com.statter.core.data.SQLiteHelper;
import com.statter.core.data.dic.model.Dictionary;
import com.statter.core.init.SQLManager;
import com.statter.data.TaskManager;
import com.statter.gateway.PoolTaskInfo;

@Component
public class ApplicationStartup implements CommandLineRunner {

	
	private static final Logger logger = Logger.getLogger(ApplicationStartup.class);


	@Autowired
	private MiningClient miningClient;

	@Autowired
	private TaskManagerCache taskManagerCache;
	@Override
	public void run(String... args) throws Exception {

		System.out.println(123123);
		init();
		TaskManager tm=taskManagerCache.getTaskManager();
		tm.setStatus(PoolTaskInfo.STATUS_MINING+"");
		taskManagerCache.putTaskManagerCache(tm);
		miningClient.Mining();

	}
	
	
	

	private static void init() throws  Exception {
		dbInit();
		intiDic();
	}
	private static void intiDic() throws Exception {
		Dictionary dic = InitUtils.intiDic();
		
		dic = InitUtils.intiMainChainDic();
		if(dic == null) {
			throw new Exception(  HiteBassConstant.INIT_DIC_BLOCKINDEX_CODE_MSG);
		}
		dic = InitUtils.intiDifficulty();
		if(dic == null) {
			throw new Exception( HiteBassConstant.INIT_DIC_BLOCKINDEX_CODE_MSG);
		}
		
	}
	
	
	private static void dbInit() throws SQLException {
		Connection connection = null;
		try {
			connection = SQLiteHelper.getConnection();
			connection.setAutoCommit(false);
			Statement statement=connection.createStatement(); 
			
			statement.executeUpdate(SQLManager.getPendingSql());  
			statement.executeUpdate(SQLManager.getFriendsSql()); 
			statement.executeUpdate(SQLManager.getCollectionSql()); 
			statement.executeUpdate(SQLManager.getTradeRecordsSql()); 
			statement.executeUpdate(SQLManager.getTokenAddressSql()); 
			statement.executeUpdate(SQLManager.getContractToContractRecordsSql()); 
			statement.executeUpdate(SQLManager.getContractToContractAddressRecordsSql()); 
			statement.executeUpdate(SQLManager.getContractWithdrawRecordsSql()); 
			statement.executeUpdate(SQLManager.getTokToContractAddressSql()); 
			statement.executeUpdate(SQLManager.getContract()); 
			statement.executeUpdate(SQLManager.getCreateContractSql()); 
			statement.executeUpdate(SQLManager.getContractDatil()); 
			statement.executeUpdate(SQLManager.getDicSql()); 
			statement.executeUpdate(SQLManager.getIpAddressSql()); 
			statement.executeUpdate(SQLManager.getWalletAddressSql());
			statement.executeUpdate(SQLManager.getTimebankFBDTTradeRecordSql());
			statement.executeUpdate(SQLManager.getTimebankTOKTradeRecordSql());
			statement.executeUpdate(SQLManager.getStorageTradeRecordsSql());
			statement.executeUpdate(SQLManager.getRepaymentTradeRecordsSql()); 
			statement.executeUpdate(SQLManager.getContractConvertRecordSql()); 
			statement.executeUpdate(SQLManager.getCreateIntelligenceContractRecordSql()); 
			statement.executeUpdate(SQLManager.getIntelligenceContractMethodRecordsSql());
			connection.commit();
		} catch (Exception e) {
			if(connection != null)
				connection.rollback();
			logger.error("init ERROR �?+e.getMessage());
		}finally {
			SQLiteHelper.close(connection);
		}
	}
	
}
