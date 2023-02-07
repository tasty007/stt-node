package com.statter.util;

public class SQLManager {
	public static final String getBlockSql(String index) {
		String sql = "CREATE TABLE IF NOT EXISTS [block" + index + "] (" + 
				"[ID] INTEGER  PRIMARY KEY AUTOINCREMENT NOT NULL," + 
				"[blockIndex] TEXT  NOT NULL," + 
				
				"[headHash] VARCHAR(300)  NOT NULL," + 
				"[path] VARCHAR(300)  UNIQUE NOT NULL," + 
				"[createTime]  VARCHAR(50)  NOT NULL," + 
				"[endHash] VARCHAR(300)  UNIQUE NOT NULL," +
				"[counter] INTEGER DEFAULT '0' NOT NULL," +
				"[randomNumber] TEXT  NOT NULL," +
				"[onMingChain] INTEGER DEFAULT '0' NOT NULL," +
				"[merkleRoot] VARCHAR(300)  UNIQUE NOT NULL," +
				"[count] INTEGER   DEFAULT '0'," +
				"[fileMD5] TEXT"+
				")";
		return sql;
	}
	public static final String getPendingSql() {
		String sql = "CREATE TABLE IF NOT EXISTS [pending] (" + 
				"[orderNo] VARCHAR(200)  UNIQUE NOT NULL," + 
				"[enterpriseEnShortName] TEXT  NOT NULL," + 
				"[content] TEXT  NULL," + 
				"[tradeType] VARCHAR(1)  NULL," + 
				"[createTime] VARCHAR(50) NOT NULL" + 
				")";
		return sql;
	}
	public static final String getCollectionSql() {
		String sql = "CREATE TABLE IF NOT EXISTS [collection] (" + 
				"[address] VARCHAR(200)  NOT NULL," + 
				"[createTime] VARCHAR(50) NOT NULL," + 
				"[remarks] VARCHAR(20)  NOT NULL" +
				")";
		return sql;
	}
	
	
	
	public static final String getWalletAddressSql() {
		String sql = "CREATE TABLE IF NOT EXISTS [walletAddress] (" + 
				"[ID] INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT," + 
				"[address] VARCHAR(200)  UNIQUE NOT NULL," + 
				"[publicKey] VARCHAR(3000)  UNIQUE NOT NULL," + 
				"[privateKey] VARCHAR(3000)  UNIQUE NOT NULL," + 
				"[amount] VARCHAR(100)  NOT NULL," + 
				"[createTime] VARCHAR(50) NOT NULL," + 
				"[type] VARCHAR(1)  NOT NULL," + 
				"[byname] VARCHAR(200) NULL"+
				")";
		return sql;
	}
	public static final String getIpAddressSql() {
		String sql = "CREATE TABLE IF NOT EXISTS [ip] (" + 
				"[ID] INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT," + 
				"[address] VARCHAR(200)  UNIQUE NOT NULL" + 
				")";
		return sql;
	}
	public static final String getTokenAddressSql() {
		String sql = "CREATE TABLE  IF NOT EXISTS [TokenWallet] (" + 
				"[id] INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT," + 
				"[address] VARCHAR(120) NOT NULL," + 
				"[contractAddress] VARCHAR(120) NOT NULL," + 
				"[amount] VARCHAR(300)  NOT NULL," + 
				"[createTime] VARCHAR(50)  NOT NULL," + 
				"[contractName] VARCHAR(300)  NOT NULL "+

				
				")";
		return sql;
	}
	
	public static final String getFriendsSql() {
		String sql = "CREATE TABLE IF NOT EXISTS [friends] (" + 
				"[Ip] VARCHAR(20)  UNIQUE NOT NULL," + 
				"[port] INTEGER  NOT NULL," + 
				"[status] INTEGER  NOT NULL," + 
				"[friendliness] INTEGER DEFAULT '100' NOT NULL," + 
				"[createTime] VARCHAR(50) NOT NULL," + 
				"[lastTime] VARCHAR(50) NOT NULL," + 
				"[superNode] VARCHAR(50) NOT NULL" + 
				")";
		return sql;
	}
	public static final String getDicSql() {
		String sql = "CREATE TABLE IF NOT EXISTS [dictionary] (" + 
				"[module] VARCHAR(20)  NOT NULL," + 
				"[key] VARCHAR(20)  NOT NULL," + 
				"[value] VARCHAR(20)  NOT NULL" + 
				")";
		return sql;
	}
	
	public static final String getContract() {
		String sql = "CREATE TABLE IF NOT EXISTS [contract] (" + 
				"[id] INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT," + 
				"[contractName] VARCHAR(200)  UNIQUE NOT NULL," + 
				"[tokenAddress] VARCHAR(200)  NULL," + 
				"[createAddress] VARCHAR(200)  NOT NULL,"+
				"[initializeAmount] VARCHAR(300)  NOT NULL," + 
				"[content] TEXT  NULL," + 
				"[icoLimitTime] VARCHAR(200)  NULL," + 
				"[enlargeLimitTime]  VARCHAR(50)  NOT NULL," +
				"[ep] VARCHAR(200)  NULL," + 
				"[ep2] VARCHAR(200)  NULL," + 
				"[unit] VARCHAR(200)  NOT NULL,"+
				"[control] VARCHAR(10)  NOT NULL,"+
				"[intervalPeriod] VARCHAR(5000)  NOT NULL,"+
				"[intervalPeriod2] VARCHAR(5000)  NOT NULL,"+
				"[createTime] VARCHAR(200)  NOT NULL,"+
				"[tradeStatus] VARCHAR(200)  NOT NULL,"+
				"[blockIndex] TEXT NOT NULL,"+
				"[confirmNumber] VARCHAR(200)  NOT NULL"+
				")";
		return sql;
	}
	
	

	
	public static final String getContractDatil() {
		String sql = "CREATE TABLE IF NOT EXISTS [ContractDatil] (" + 
				"[id] INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT," + 
				"[contractName] VARCHAR(200)  UNIQUE NOT NULL," + 
				"[tokenAddress] VARCHAR(200)  NULL," + 
				"[createAddress] VARCHAR(200)  NOT NULL,"+
				"[initializeAmount] VARCHAR(300)  NOT NULL," + 
				"[contractBalance] VARCHAR(300)  NOT NULL," + 
				"[tokBalance] VARCHAR(300)  NOT NULL," + 
				"[content] TEXT  NULL," + 
				"[icoLimitTime] VARCHAR(200)  NULL," + 
				"[enlargeLimitTime] VARCHAR(50)  NOT NULL," +
				"[ep] VARCHAR(200)  NULL," + 
				"[ep2] VARCHAR(200)  NULL," + 
				"[unit] VARCHAR(200)  NOT NULL,"+
				"[control] VARCHAR(10)  NOT NULL,"+
				"[intervalPeriod] VARCHAR(5000)  NOT NULL,"+
				"[intervalPeriod2] VARCHAR(5000)  NOT NULL,"+
				"[createTime] VARCHAR(200)  NOT NULL"+
				
				")";
		return sql;
	}

	
	
	
	public static final String getTradeRecordsSql() {
		String sql = "CREATE TABLE IF NOT EXISTS [tradeRecords] (" + 
				"[blockIndex] TEXT ," +
				"[tradeNo] VARCHAR(200)  UNIQUE NOT NULL," + 
				"[from] VARCHAR(200)  NOT NULL," + 
				"[amount] VARCHAR(100)  NOT NULL," + 
				"[to] VARCHAR(200) NOT NULL  ," + 
				"[gas] VARCHAR(10)  NOT NULL," + 
				"[tradeTime] VARCHAR(50)  NOT NULL," + 
				"[confirmNumber] INTEGER  NULL," +
				"[tradeStatus] VARCHAR(10)  NULL," +
				"[amountType] VARCHAR(2)  NULL," +
				
				"[statusDescription] VARCHAR(100)  NULL" +
				")";
		return sql;
	}
	
	public static final String getContractToContractRecordsSql() {
		String sql = "CREATE TABLE IF NOT EXISTS [contractToContractRecords] (" + 
				"[blockIndex] TEXT ," +
				"[tradeNo] VARCHAR(200)  UNIQUE NOT NULL," + 
				"[from] VARCHAR(200)  NOT NULL," + 
				"[amount] VARCHAR(100)  NOT NULL," + 
				"[to] VARCHAR(200) NOT NULL  ," + 
				"[gas] VARCHAR(10)  NOT NULL," + 
				"[tradeTime] VARCHAR(50)  NOT NULL," + 
				"[confirmNumber] INTEGER  NULL," +
				"[tradeStatus] VARCHAR(10)  NULL," +
				"[amountType] VARCHAR(2)  NULL," +
				"[contractName] VARCHAR(200)  NULL," +
				"[statusDescription] VARCHAR(100)  NULL" +
				")";
		return sql;
	}
	public static final String getCreateContractSql() {
		String sql = "CREATE TABLE IF NOT EXISTS [CreateContractRecords] (" + 
				"[tradeNo] VARCHAR(200)  UNIQUE NOT NULL,"
				+ "[tradeAmount] VARCHAR(100)  NOT NULL,"
				+ "[contractNumber] VARCHAR(200)  NULL,"
				+ "[unit] VARCHAR(10) NULL,"
				+ "[contractAddress] VARCHAR(200)  NOT NULL,"
				+ "[paymentAddress] VARCHAR(200)  NOT NULL,"
				+ "[incomeAddress] VARCHAR(200)  NOT NULL,"
				+ "[gas] VARCHAR(10) NOT NULL,"
				+ "[initContractAmount] VARCHAR(200)  NOT NULL,"
				+ "[icoLimitTime]  VARCHAR(50)  NOT NULL,"
				+ "[enlargeLimitTime]  VARCHAR(50)  NOT NULL,"
				+ "[ep] VARCHAR(10)  NOT NULL,"
				+ "[ep2] VARCHAR(10)  NOT NULL,"
				+ "[tradeTime] VARCHAR(50)  NOT NULL,"
				+ "[content] TEXT,"
				+ "[control]  VARCHAR(2) NULL,"
				+ "[intervalPeriod],"
				+ "[intervalPeriod2],"
				+ "[blockIndex] TEXT,"
				+ "[confirmNumber],"
				+ "[tradeStatus] VARCHAR(10)  NULL,"
				+ "[amountType] VARCHAR(2)  NULL,"
				+ "[statusDescription]  VARCHAR(100)  NULL"
				+ ")";
		return sql;
	}
}
