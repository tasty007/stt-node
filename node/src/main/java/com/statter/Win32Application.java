package com.statter;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.google.gson.Gson;
import com.statter.core.common.BtcConstant;
import com.statter.core.common.DataUtils;
import com.statter.core.common.InitUtils;
import com.statter.core.common.IpUtils;
import com.statter.core.common.exception.ErrorInfo;
import com.statter.core.common.exception.TokException;
import com.statter.core.common.http.HttpClientUtils;
import com.statter.core.data.SQLiteHelper;
import com.statter.core.data.block.model.Block;
import com.statter.core.data.block.service.impl.BlockServiceImpl;
import com.statter.core.data.dic.dicService.impl.DicServiceImpl;
import com.statter.core.data.dic.model.Dictionary;
import com.statter.core.data.friends.model.Friends;
import com.statter.core.data.friends.service.impl.FriendServiceImpl;
import com.statter.core.data.p2p.NoticeParams;
import com.statter.core.init.SQLManager;


@SpringBootApplication
@EnableScheduling
@ServletComponentScan
public class Win32Application extends SpringBootServletInitializer{

	private static final Logger logger = Logger.getLogger(Win32Application.class);
	
	public static void main(String[] args) {
		SpringApplication.run(Win32Application.class, args);
	}
	 @Override
		protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
	        return application.sources(Win32Application.class);
	    }
		
		@Bean	
		public EmbeddedServletContainerCustomizer containerCustomizer() {
			return new EmbeddedServletContainerCustomizer() {
				@Override
				public void customize(ConfigurableEmbeddedServletContainer container) {				
				container.setSessionTimeout(15000);
				}
			};
		}
	private static void init() throws SQLException, ClassNotFoundException {
		dbInit();
		addFields();
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
			logger.error("init ERROR  ?+e.getMessage());
		}finally {
			SQLiteHelper.close(connection);
		}
	}
	private static void addFields() throws SQLException {
		Connection connection = null;
		try {
			connection = SQLiteHelper.getConnection();
			connection.setAutoCommit(false);
			Statement statement=connection.createStatement(); 
			try {
				statement.executeUpdate(SQLManager.addTradeRecordsField());
				connection.commit();
			} catch (Exception e) {
				if(connection != null)
					connection.rollback();
				logger.error("init ERROR  ?+e.getMessage());
			} 
			try {
				statement.executeUpdate(SQLManager.addWalletField()); 
				connection.commit();
			} catch (Exception e) {
				if(connection != null)
					connection.rollback();
				logger.error("init ERROR  ?+e.getMessage());
			}
			try {
				statement.executeUpdate(SQLManager.addPendingField()); 
				connection.commit();
			} catch (Exception e) {
				if(connection != null)
					connection.rollback();
				logger.error("init ERROR  ?+e.getMessage());
			}
		} catch (Exception e) {
			logger.error("init ERROR  ?+e.getMessage());
		} finally{
			SQLiteHelper.close(connection);
		}
	}
	
	public static void update() throws SQLException {
		
		FriendServiceImpl impl = new FriendServiceImpl();
		List<Friends>ips = FriendServiceImpl.queryAllFriends();
		Dictionary dic = DicServiceImpl.queryDic(Dictionary.MODUAL_BLOCK, Dictionary.BLOCKINDEX_KEY);
		int index=0;
		if(dic==null) {
		
			dic = new Dictionary();
			dic.setModule(Dictionary.MODUAL_BLOCK);
			dic.setKey(Dictionary.BLOCKINDEX_KEY);
			dic.setValue("");
			
		} 
		index=Integer.parseInt(dic.getValue());
		System.out.println(index);
		String hash=null;
		while(true) {
				boolean check=false;
				NoticeParams noticeParams = new NoticeParams();
				noticeParams.setBn( String.valueOf(index));
				Map<String , String> map = new HashMap<String , String>();
				map.put("bn", String.valueOf(index));
				Map <String ,String> maps = getBlock(ips.get(0).getIp(),map);
				if(maps==null ) {
					break;
				}
				String strBlock=maps.get("block");
				if(strBlock==null ) {
					
					break;
				}
				
				Block block = new Gson().fromJson(strBlock, Block.class);
				String headHash=block.getHeadHash();
				if(hash==null) {
					check=true;
				}
				if(hash!=null && hash.endsWith(headHash)) {
					check=true;
				}
				if(check) {
					BlockServiceImpl.checkBlockTable(block.getBlockIndex());
					BlockServiceImpl.save(block);
					DataUtils.writeFile(block.getPath(), maps.get("blockFile"));
					dic.setValue(String.valueOf(index));
					DicServiceImpl.update(dic);
					System.out.println(block.getPath());
					System.out.println(DataUtils.getRelativePath(block.getPath()));
				
					
					
					index++;
				}else {
					
				}
				
				hash=block.getEndHash();
			}
		
	}
	
	public static  Map <String ,String> getBlock(String ip,Map<String , String>   map ) {
		
		Map <String ,String> maps  =HttpClientUtils.UpdateDoPost("http:
		
		
		return maps;
	}
	private static void intiDic() throws TokException{
		Dictionary dic = InitUtils.intiDic();
		if(dic == null) {
			throw new TokException(ErrorInfo.INIT_DIC_BLOCKINDEX_CODE, ErrorInfo.INIT_DIC_BLOCKINDEX_CODE_MSG);
		}
		dic = InitUtils.intiMainChainDic();
		if(dic == null) {
			throw new TokException(ErrorInfo.INIT_DIC_BLOCKINDEX_CODE, ErrorInfo.INIT_DIC_BLOCKINDEX_CODE_MSG);
		}
		dic = InitUtils.intiDifficulty();
		if(dic == null) {
			throw new TokException(ErrorInfo.INIT_DIC_BLOCKINDEX_CODE, ErrorInfo.INIT_DIC_BLOCKINDEX_CODE_MSG);
		}
		dic = InitUtils.intiProfit();
		if(dic == null) {
			throw new TokException(ErrorInfo.INIT_DIC_BLOCKINDEX_CODE, ErrorInfo.INIT_DIC_BLOCKINDEX_CODE_MSG);
		}
		dic=InitUtils.intiART();
		if(dic == null) {
			throw new TokException(ErrorInfo.INIT_DIC_BLOCKINDEX_CODE, ErrorInfo.INIT_DIC_BLOCKINDEX_CODE_MSG);
		}
	}
	@Deprecated
	private static void initMode() {
		boolean b = IpUtils.isInternalIp();
		
		if(b) {
			
			DataUtils.client_mode = BtcConstant.CLIENT_MODE_UPDATE;
		}else {
			
			DataUtils.client_mode = BtcConstant.CLIENT_MODE_MINING;
		}
		System.out.println();
	}
}
