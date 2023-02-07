package com.statter.controller;


import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.statter.core.common.BtcConstant;
import com.statter.core.common.CacheUtils;
import com.statter.core.common.DataUtils;
import com.statter.core.common.InitUtils;
import com.statter.core.common.IpUtils;
import com.statter.core.common.Mining;
import com.statter.core.common.block.BlockBaseHandler;
import com.statter.core.common.block.BlockBaseUtils;
import com.statter.core.common.block.ValidateHandler;
import com.statter.core.common.exception.ErrorInfo;
import com.statter.core.common.exception.TokException;
import com.statter.core.data.block.model.Block;
import com.statter.core.data.block.model.BlockAddressList;
import com.statter.core.data.block.model.BlockAllAddressList;
import com.statter.core.data.block.model.IntervalPeriod;
import com.statter.core.data.block.model.MiningInfo;
import com.statter.core.data.block.service.impl.BlockServiceImpl;
import com.statter.core.data.dic.dicService.impl.DicServiceImpl;
import com.statter.core.data.dic.model.Dictionary;
import com.statter.core.data.friends.service.impl.FriendServiceImpl;
import com.statter.core.data.p2p.trade.TradeBody;
import com.statter.core.data.p2p.trade.TradeBodyImpl;
import com.statter.core.data.p2p.trade.TradeBodyPool;
import com.statter.core.data.p2p.trade.b2b.ContractConvertTradeBody;
import com.statter.core.data.p2p.trade.senior.model.MethodParams;
import com.statter.core.data.p2p.trade.senior.trade.ContractToContractAddressTradebody;
import com.statter.core.data.p2p.trade.senior.trade.ContractToContractTradeBody;
import com.statter.core.data.p2p.trade.senior.trade.ContractWithdrawTradebody;
import com.statter.core.data.p2p.trade.senior.trade.CreateContractTradeBody;
import com.statter.core.data.p2p.trade.senior.trade.CreateIntelligenceContractTradeBody;
import com.statter.core.data.p2p.trade.senior.trade.IntelligenceContractMethodTradeBody;
import com.statter.core.data.p2p.trade.senior.trade.TokToContractAddressTradebody;
import com.statter.core.data.p2p.trade.timebank.LoanByFBDTTradeBody;
import com.statter.core.data.p2p.trade.timebank.LoanByTOKTradeBody;
import com.statter.core.data.p2p.trade.timebank.RepaymentTradeBody;
import com.statter.core.data.p2p.trade.timebank.StorageTradeBody;
import com.statter.core.data.p2p.trade.timebank.TradeStatusTradeBody;
import com.statter.core.data.pending.model.Pending;
import com.statter.core.data.pending.service.impl.PendingServiceImpl;
import com.statter.core.data.token.model.Contract;
import com.statter.core.data.token.service.impl.ContractServiceImpl;
import com.statter.core.encrypt.EncryptUtil;
import com.statter.core.thread.MiningThread;

@SpringBootApplication
@RestController
public class TradeController  extends ValidateHandler{
	
	@PostMapping(value=BtcConstant.BLOCK_TRADE_URI)
	@ResponseBody
	public void trade(@RequestBody Map<String, Object> data) {
		if(BlockBaseHandler.currentMode.equals(BtcConstant.MODE_UPDATE)) {
			
			return;
		}
		String ip = "";
		try {
			
			if(data == null) {
				return;
			}
			String hostAddress = IpUtils.getRemoteIpAddr();
			ip = data.get("ip") != null? data.get("ip").toString():"";
			String tradeInfo = data.get("tradeInfo") !=null ?  data.get("tradeInfo").toString() : "";
			if(StringUtils.isBlank(ip) || StringUtils.isBlank(tradeInfo)) {
				
				return;
			}
			TradeBody tb = TradeBodyImpl.mapToTradeBody((Map)data.get("tradeInfo"));
			if(tb == null) {
				
				return;
			}
			if(StringUtils.isBlank(tb.getFrom()) 
					|| StringUtils.isBlank(tb.getTo()) 
					|| tb.getTradeAmount() == null 
					|| tb.getGas() ==null 
					|| tb.getTo().trim().length() !=BtcConstant.TOKEN_ADDRESS_LENGTH ) {
				throw new TokException(ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE, ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE);
			}
			String now=DataUtils.getCurrentTime();
			checkTime(now, tb.getTradeTime());
			
			checkExist(tb);
			
			TradeUtils.validateTradeNo(tb);
			
			TradeUtils.validateAddress(tb);
			
			TradeUtils.validateSign(tb);
			
			validateGas(tb);
			
			genPending(tb);
			
			TradeUtils.sends(tb);
			
			FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_ADD);
		
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			
			if(StringUtils.isNotBlank(ip)) {
				
				FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_LESS);
			}
		}
		
	}

	@PostMapping(value=BtcConstant.WS_CREATECONTRACT_URI)
	@ResponseBody
	public void createcontract(@RequestBody Map<String, Object> data) {
		if(BlockBaseHandler.currentMode.equals(BtcConstant.MODE_UPDATE)) {
			
			return;
		}
		String ip = "";
		try {
			
			
			if(data == null) {
				return;
			}
			String hostAddress = IpUtils.getRemoteIpAddr();
			ip = data.get("ip") != null? data.get("ip").toString():"";
			String tradeInfo = data.get("tradeInfo") !=null ?  data.get("tradeInfo").toString() : "";
			if(StringUtils.isBlank(ip) || StringUtils.isBlank(tradeInfo)) {
				
				return;
			}
			CreateContractTradeBody tb = TradeBodyImpl.mapToCreateContractTradeBody((Map)data.get("tradeInfo"));
			if(tb == null) {
				
				return;
			}
			
			if(StringUtils.isBlank(tb.getPaymentAddress()) 
					|| StringUtils.isBlank(tb.getContractAddress()) 
					|| tb.getContractAddress() == null 
					|| tb.getGas() ==null 
					|| new BigDecimal(tb.getTradeAmount()).compareTo(BigDecimal.ZERO) <= 0 
					|| new BigDecimal(tb.getGas()).compareTo(BtcConstant.MIN_GAS) < 0  
					|| tb.getPaymentAddress().trim().length() != BtcConstant.TOKEN_ADDRESS_LENGTH 
					|| !tb.getContractAddress().equals(DataUtils.genContractAddress(tb.getPaymentAddress(), tb.getContractNumber()))
					|| !BtcConstant.CONTRACT_CREATE_PAY_ADDRESS.equals(tb.getIncomeAddress())) {
				throw new TokException(ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE, ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE);
			}
			
			TradeBody tby= new TradeBody();
			tby.setTradeNo(tb.getTradeNo());
			tby.setPublicKey(tb.getPublicKey());
			tby.setFrom(tb.getPaymentAddress());
			tby.setGas(tb.getGas());
			
			
			String now=DataUtils.getCurrentTime();
			checkTime(now, tb.getTradeTime());
			
			checkExist(tby);
			
			TradeUtils.validateTradeNo(tb);
			
			TradeUtils.validateAddress(tb);
			
			TradeUtils.validateSign(tb);
			
			TradeUtils.validateGas(tb);
			
			genPending(tb);
			
			TradeUtils.sends(tb);
			
			FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_ADD);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			
			if(StringUtils.isNotBlank(ip)) {
				
				FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_LESS);
			}
		}
		
	}
	@PostMapping(value=BtcConstant.WS_CONTRACTTOCONTRACT_URI)
	@ResponseBody
	public void contracttocontract(@RequestBody Map<String, Object> data) {
		if(BlockBaseHandler.currentMode.equals(BtcConstant.MODE_UPDATE)) {
			
			return;
		}
		String ip = "";
		try {
			
			if(data == null) {
				return;
			}
			String hostAddress = IpUtils.getRemoteIpAddr();
			ip = data.get("ip") != null? data.get("ip").toString():"";
			String tradeInfo = data.get("tradeInfo") !=null ?  data.get("tradeInfo").toString() : "";
			if(StringUtils.isBlank(ip) || StringUtils.isBlank(tradeInfo)) {
				
				return;
			}
			ContractToContractTradeBody tb = TradeBodyImpl.mapToContractToContractTradeBody((Map)data.get("tradeInfo"));
			if(tb == null) {
				
				return;
			}
			if(StringUtils.isBlank(tb.getFrom()) 
					|| StringUtils.isBlank(tb.getTo()) 
					|| tb.getTradeAmount() == null 
					|| tb.getGas() ==null 
					|| new BigDecimal(tb.getTradeAmount()).compareTo(BigDecimal.ZERO) <= 0 
					|| new BigDecimal(tb.getGas()).compareTo(BtcConstant.MIN_GAS) < 0 
					|| BtcConstant.CONTRACT_NAME_MIN_LENGTH > tb.getContractNumber().trim().length()
					|| BtcConstant.CONTRACT_NAME_MAX_LENGTH < tb.getContractNumber().trim().length()
					|| !tb.getContractAddress().startsWith(BtcConstant.CONTRACT_ADDRESS_PRE)
					|| !tb.getContractAddress().endsWith(tb.getContractNumber())
					|| tb.getTo().trim().length() < BtcConstant.TOKEN_ADDRESS_LENGTH 
					|| tb.getTo().trim().length() > BtcConstant.TOKEN_ADDRESS_LENGTH*2 ) {
				throw new TokException(ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE, ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE);
			}
			
			TradeBody tby= new TradeBody();
			tby.setTradeNo(tb.getTradeNo());
			tby.setPublicKey(tb.getPublicKey());
			tby.setFrom(tb.getFrom());
			tby.setGas(tb.getGas());
			String now=DataUtils.getCurrentTime();
			checkTime(now, tb.getTradeTime());
			
			
			checkExist(tby);
			
			TradeUtils.validateTradeNo(tb);
			
			TradeUtils.validateAddress(tb);
			
			TradeUtils.validateSign(tb);
			
			TradeUtils.validateGas(tb);
			
			genPending(tb);
			
			TradeUtils.sends(tb);
			
			FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_ADD);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			
			if(StringUtils.isNotBlank(ip)) {
				
				FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_LESS);
			}
		}
		
	}
	
	
	
	@PostMapping(value=BtcConstant.WS_CONTRACTWITHDRAW_URI)
	@ResponseBody
	public void contractwithdraw(@RequestBody Map<String, Object> data) {
		if(BlockBaseHandler.currentMode.equals(BtcConstant.MODE_UPDATE)) {
			
			return;
		}
		String ip = "";
		try {
			
			if(data == null) {
				return;
			}
			String hostAddress = IpUtils.getRemoteIpAddr();
			ip = data.get("ip") != null? data.get("ip").toString():"";
			String tradeInfo = data.get("tradeInfo") !=null ?  data.get("tradeInfo").toString() : "";
			if(StringUtils.isBlank(ip) || StringUtils.isBlank(tradeInfo)) {
				
				return;
			}
			ContractWithdrawTradebody tb = TradeBodyImpl.mapToContractWithdrawTradebody((Map)data.get("tradeInfo"));
			if(tb == null) {
				
				return;
			}
			if(StringUtils.isBlank(tb.getPaymentAddress()) 
					|| StringUtils.isBlank(tb.getContractAddress())
					|| tb.getGas() ==null 
					|| new BigDecimal(tb.getTradeAmount()).compareTo(BigDecimal.ZERO) <= 0 
					|| new BigDecimal(tb.getGas()).compareTo(BtcConstant.MIN_GAS) < 0 
					|| tb.getPaymentAddress().length()!=BtcConstant.TOKEN_ADDRESS_LENGTH
					|| !tb.getContractAddress().equals(DataUtils.genContractAddress(tb.getPaymentAddress(), tb.getContractNumber()))) {
				throw new TokException(ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE, ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE);
			}
			
			TradeBody tby= new TradeBody();
			tby.setTradeNo(tb.getTradeNo());
			tby.setPublicKey(tb.getPublicKey());
			tby.setFrom(tb.getPaymentAddress());
			tby.setGas(tb.getGas());
			
			
			String now=DataUtils.getCurrentTime();
			
			checkTime(now, tb.getTradeTime());
			
			checkExist(tby);
			
			TradeUtils.validateTradeNo(tb);
			
			TradeUtils.validateAddress(tb);
			
			TradeUtils.validateSign(tb);
			
			TradeUtils.validateGas(tb);
			
			genPending(tb);
			
			TradeUtils.sends(tb);
			
			FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_ADD);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			
			if(StringUtils.isNotBlank(ip)) {
				
				FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_LESS);
			}
		}
		
	}
	@PostMapping(value=BtcConstant.WS_TOKTOCONTRACTADDRESS_URI)
	@ResponseBody
	public void toktocontractaddress(@RequestBody Map<String, Object> data) {
		if(BlockBaseHandler.currentMode.equals(BtcConstant.MODE_UPDATE)) {
			
			return;
		}
		String ip = "";
		try {
			
			if(data == null) {
				return;
			}
			String hostAddress = IpUtils.getRemoteIpAddr();
			ip = data.get("ip") != null? data.get("ip").toString():"";
			String tradeInfo = data.get("tradeInfo") !=null ?  data.get("tradeInfo").toString() : "";
			if(StringUtils.isBlank(ip) || StringUtils.isBlank(tradeInfo)) {
				
				return;
			}
			TokToContractAddressTradebody tb = TradeBodyImpl.mapToTokToContractAddressTradebody((Map)data.get("tradeInfo"));
			if(tb == null) {
				
				return;
			}
			if(StringUtils.isBlank(tb.getFrom()) 
					|| StringUtils.isBlank(tb.getContractAddress()) 
					|| StringUtils.isBlank(tb.getContractNumber())
					|| tb.getContractAddress() == null 
					|| tb.getGas() ==null 
					|| new BigDecimal(tb.getTradeFromAmount()).compareTo(BigDecimal.ZERO) <= 0 
					|| new BigDecimal(tb.getGas()).compareTo(BtcConstant.MIN_GAS) < 0 
					|| BtcConstant.CONTRACT_NAME_MIN_LENGTH > tb.getContractNumber().trim().length()
					|| BtcConstant.CONTRACT_NAME_MAX_LENGTH < tb.getContractNumber().trim().length()
					|| !tb.getContractAddress().startsWith(BtcConstant.CONTRACT_ADDRESS_PRE)
					|| !tb.getContractAddress().endsWith(tb.getContractNumber())) {
				throw new TokException(ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE, ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE);
			}
			
			Contract c = ContractServiceImpl.queryContractByContractNumber(tb.getContractNumber());
			if(c != null) {
				String control = c.getControl();
				
				if('1' == control.charAt(0)) {
					String time = c.getIcoLimitTime();
					Date icoLimitTime = DateUtils.parseDate(time, BtcConstant.DATE_FORMAT);
					Date tradeTime = DateUtils.parseDate(tb.getTradeTime(), BtcConstant.DATE_FORMAT);
					if(tradeTime.after(icoLimitTime)) {
						
						throw new TokException(ErrorInfo.CONTRACT_TRADE_CONTRACT_END, ErrorInfo.CONTRACT_TRADE_CONTRACT_END_MSG);
					}
				}
			}else{
				throw new TokException(ErrorInfo.CONTRACT_TRADE_NOT_EXIST, ErrorInfo.CONTRACT_TRADE_NOT_EXIST_MSG);
			}
			
			Contract contract = ContractServiceImpl.queryContractByContractNumber(tb.getContractNumber());
			String control = contract.getControl();
			BigDecimal amount = new BigDecimal(tb.getTradeFromAmount());
			if('1' == control.charAt(0)) {
				IntervalPeriod intervalPeriod = BlockBaseUtils.getIntervalPeriod(amount, contract.getIntervalPeriod());
				if(intervalPeriod==null) {
					throw new TokException(ErrorInfo.CONTRACT_TRADE_ICO_MONEY, ErrorInfo.CONTRACT_TRADE_ICO_MONEY_MSG);
				}
			}
			
			TradeBody tby= new TradeBody();
			tby.setTradeNo(tb.getTradeNo());
			tby.setPublicKey(tb.getPublicKey());
			tby.setFrom(tb.getFrom());
			tby.setGas(tb.getGas());
			
			
			String now=DataUtils.getCurrentTime();
			
			checkTime(now, tb.getTradeTime());
			checkExist(tby);
			
			TradeUtils.validateTradeNo(tb);
			
			TradeUtils.validateAddress(tb);
			
			TradeUtils.validateSign(tb);
			
			TradeUtils.validateGas(tb);
			
			genPending(tb);
			
			TradeUtils.sends(tb);
			
			FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_ADD);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			
			if(StringUtils.isNotBlank(ip)) {
				
				FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_LESS);
			}
		}
		
	}

	@PostMapping(value=BtcConstant.WS_CONTRACTTOCONTRACTADDRESS_URI,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public void contracttocontractaddress(@RequestBody Map<String, Object> data) {
		if(BlockBaseHandler.currentMode.equals(BtcConstant.MODE_UPDATE)) {
			
			return;
		}
		String ip = "";
		try {
			
			if(data == null) {
				return;
			}
			String hostAddress = IpUtils.getRemoteIpAddr();
			ip = data.get("ip") != null? data.get("ip").toString():"";
			String tradeInfo = data.get("tradeInfo") !=null ?  data.get("tradeInfo").toString() : "";
			if(StringUtils.isBlank(ip) || StringUtils.isBlank(tradeInfo)) {
				
				return;
			}
			ContractToContractAddressTradebody tb = TradeBodyImpl.mapToContractToContractAddressTradebody((Map)data.get("tradeInfo"));
			if(tb == null) {
				
				return;
			}
			if(StringUtils.isBlank(tb.getFrom()) 
					|| StringUtils.isBlank(tb.getContractAddress()) 
					|| tb.getTradeFromAmount() == null 
					|| tb.getGas() ==null 
					|| new BigDecimal(tb.getTradeFromAmount()).compareTo(BigDecimal.ZERO) <= 0 
					|| new BigDecimal(tb.getGas()).compareTo(new BigDecimal("0.0006")) < 0 
					|| BtcConstant.CONTRACT_NAME_MIN_LENGTH > tb.getContractNumber().trim().length()
					|| BtcConstant.CONTRACT_NAME_MAX_LENGTH < tb.getContractNumber().trim().length()
					|| !tb.getContractAddress().startsWith(BtcConstant.CONTRACT_ADDRESS_PRE)
					|| !tb.getContractAddress().endsWith(tb.getContractNumber())) {
				throw new TokException(ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE, ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE);
			}
			
			Contract c = ContractServiceImpl.queryContractByContractNumber(tb.getContractNumber());
			if(c != null) {
				String control = c.getControl();
				
				if('1' == control.charAt(1)) {
					String time = c.getEnlargeLimitTime();
					Date contractLimitTime = DateUtils.parseDate(time, BtcConstant.DATE_FORMAT);
					Date tradeTime = DateUtils.parseDate(tb.getTradeTime(), BtcConstant.DATE_FORMAT);
					if(tradeTime.after(contractLimitTime)) {
						
						throw new TokException(ErrorInfo.CONTRACT_TRADE_CONTRACT_END, ErrorInfo.CONTRACT_TRADE_CONTRACT_END_MSG);
					}
				}
			}else{
				throw new TokException(ErrorInfo.CONTRACT_TRADE_NOT_EXIST, ErrorInfo.CONTRACT_TRADE_NOT_EXIST_MSG);
			}
			
			TradeBody tby= new TradeBody();
			tby.setTradeNo(tb.getTradeNo());
			tby.setPublicKey(tb.getPublicKey());
			tby.setFrom(tb.getFrom());
			tby.setGas(tb.getGas());
			
			
			
			checkExist(tby);
			
			TradeUtils.validateTradeNo(tb);
			
			TradeUtils.validateAddress(tb);
			
			TradeUtils.validateSign(tb);
			
			TradeUtils.validateGas(tb);
			
			genPending(tb);
			
			TradeUtils.sends(tb);
			
			FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_ADD);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			
			if(StringUtils.isNotBlank(ip)) {
				
				FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_LESS);
			}
		}
		
	}
	
	@PostMapping(value=BtcConstant.WS_STORAGE_URI,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public void contractStorage(@RequestBody Map<String, Object> data) {
		if(BlockBaseHandler.currentMode.equals(BtcConstant.MODE_UPDATE)) {
			
			return;
		}
		String ip = "";
		try {
			
			if(data == null) {
				return;
			}
			String hostAddress = IpUtils.getRemoteIpAddr();
			ip = data.get("ip") != null? data.get("ip").toString():"";
			String tradeInfo = data.get("tradeInfo") !=null ?  data.get("tradeInfo").toString() : "";
			if(StringUtils.isBlank(ip) || StringUtils.isBlank(tradeInfo)) {
				
				return;
			}
			StorageTradeBody tb = TradeBodyImpl.mapToStorageTradeBody((Map)data.get("tradeInfo"));
			if(tb == null) {
				
				return;
			}
			if(StringUtils.isBlank(tb.getUserAddress()) 
					|| StringUtils.isBlank(tb.getTbAddress()) 
					|| StringUtils.isBlank(tb.getTbPublicKey()) 
					|| StringUtils.isBlank(tb.getUserPublicKey()) 
					|| StringUtils.isBlank(tb.getStorageTime()) 
					|| StringUtils.isBlank(tb.getTradeNo()) 
					|| tb.getUserSign() == null 
					|| tb.getGas() ==null 
					|| new BigDecimal(tb.getStorageAmount()).compareTo(BigDecimal.ZERO) <= 0 
					|| new BigDecimal(tb.getGas()).compareTo(new BigDecimal("0.0006")) < 0 
					||  new BigDecimal(tb.getRate()).compareTo(BigDecimal.ZERO) <= 0
					|| tb.getStatus()==null) {
				throw new TokException(ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE, ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE);
			}
			
			
			TradeBody tby= new TradeBody();
			tby.setTradeNo(tb.getTradeNo());
			tby.setPublicKey(tb.getTbPublicKey());
			tby.setFrom(tb.getTbAddress());
			tby.setGas(tb.getGas());
			
			String now=DataUtils.getCurrentTime();
			
			checkTime(now, tb.getStorageTime());
			
			checkExist(tby);
			
			TradeUtils.validateTradeNo(tb);
			
			TradeUtils.validateAddress(tb);		
			TradeUtils.validateTbAddress(tb);
			
			TradeUtils.validateSign(tb);
			
			TradeUtils.validateGas(tb);
			
			genPending(tb);
			
			TradeUtils.sends(tb);
			
			FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_ADD);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			
			if(StringUtils.isNotBlank(ip)) {
				
				FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_LESS);
			}
		}
		
	}
	
	@PostMapping(value=BtcConstant.WS_LOANBYTOK_URI,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public void contractLoanByTOKTradeBody(@RequestBody Map<String, Object> data) {
		if(BlockBaseHandler.currentMode.equals(BtcConstant.MODE_UPDATE)) {
			
			return;
		}
		String ip = "";
		try {
			
			if(data == null) {
				return;
			}
			String hostAddress = IpUtils.getRemoteIpAddr();
			ip = data.get("ip") != null? data.get("ip").toString():"";
			String tradeInfo = data.get("tradeInfo") !=null ?  data.get("tradeInfo").toString() : "";
			if(StringUtils.isBlank(ip) || StringUtils.isBlank(tradeInfo)) {
				
				return;
			}
			LoanByTOKTradeBody tb = TradeBodyImpl.mapToLoanTradeBody((Map)data.get("tradeInfo"));
			if(tb == null) {
				
				return;
			}
			if(StringUtils.isBlank(tb.getUserAddress()) 
					|| StringUtils.isBlank(tb.getTbAddress()) 
					|| StringUtils.isBlank(tb.getTbPublicKey()) 
					|| StringUtils.isBlank(tb.getUserPublicKey()) 
					|| StringUtils.isBlank(tb.getTradeNo()) 
					|| tb.getUserSign() == null 
					|| tb.getGas() ==null 
					|| new BigDecimal(tb.getLoanAmount()).compareTo(BigDecimal.ZERO) <= 0 
					|| new BigDecimal(tb.getGas()).compareTo(new BigDecimal("0.0006")) < 0 
					||  new BigDecimal(tb.getRate()).compareTo(BigDecimal.ZERO) <= 0) {
				throw new TokException(ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE, ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE);
			}
			
			
			TradeBody tby= new TradeBody();
			tby.setTradeNo(tb.getTradeNo());
			tby.setPublicKey(tb.getTbPublicKey());
			tby.setFrom(tb.getTbAddress());
			tby.setGas(tb.getGas());
			
			String now=DataUtils.getCurrentTime();
			
			checkTime(now, tb.getStarTime());
			
			checkExist(tby);
			
			TradeUtils.validateTradeNo(tb);
			
			TradeUtils.validateAddress(tb);		
			TradeUtils.validateTbAddress(tb);
			
			TradeUtils.validateSign(tb);
			
			TradeUtils.validateGas(tb);
			
			genPending(tb);
			
			TradeUtils.sends(tb);
			
			FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_ADD);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			
			if(StringUtils.isNotBlank(ip)) {
				
				FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_LESS);
			}
		}
		
	}
	
	
	@PostMapping(value=BtcConstant.WS_LOANBYFBDT_URI,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public void contractLoanByFBDTTradeBody(@RequestBody Map<String, Object> data) {
		if(BlockBaseHandler.currentMode.equals(BtcConstant.MODE_UPDATE)) {
			
			return;
		}
		String ip = "";
		try {
			
			if(data == null) {
				return;
			}
			String hostAddress = IpUtils.getRemoteIpAddr();
			ip = data.get("ip") != null? data.get("ip").toString():"";
			String tradeInfo = data.get("tradeInfo") !=null ?  data.get("tradeInfo").toString() : "";
			if(StringUtils.isBlank(ip) || StringUtils.isBlank(tradeInfo)) {
				
				return;
			}
			LoanByFBDTTradeBody tb = TradeBodyImpl.mapToLoanByFBDTTradeBody((Map)data.get("tradeInfo"));
			if(tb == null) {
				
				return;
			}
			if(StringUtils.isBlank(tb.getUserAddress()) 
					|| StringUtils.isBlank(tb.getTbAddress()) 
					|| StringUtils.isBlank(tb.getTbPublicKey()) 
					|| StringUtils.isBlank(tb.getUserPublicKey()) 
					|| StringUtils.isBlank(tb.getTradeNo()) 
					|| tb.getUserSign() == null 
					|| tb.getGas() ==null 
					|| new BigDecimal(tb.getLoanAmount()).compareTo(BigDecimal.ZERO) <= 0 
					|| new BigDecimal(tb.getGas()).compareTo(new BigDecimal("0.0006")) < 0 
					||  new BigDecimal(tb.getRate()).compareTo(BigDecimal.ZERO) <= 0) {
				throw new TokException(ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE, ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE);
			}
			
			
			TradeBody tby= new TradeBody();
			tby.setTradeNo(tb.getTradeNo());
			tby.setPublicKey(tb.getTbPublicKey());
			tby.setFrom(tb.getTbAddress());
			tby.setGas(tb.getGas());
			
			String now=DataUtils.getCurrentTime();
			
			checkTime(now, tb.getStarTime());
			
			checkExist(tby);
			
			TradeUtils.validateTradeNo(tb);
			
			TradeUtils.validateAddress(tb);		
			TradeUtils.validateTbAddress(tb);
			
			TradeUtils.validateSign(tb);
			
			TradeUtils.validateGas(tb);
			
			genPending(tb);
			
			TradeUtils.sends(tb);
			
			FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_ADD);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			
			if(StringUtils.isNotBlank(ip)) {
				
				FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_LESS);
			}
		}
		
	}
	
	
	
	
	@PostMapping(value=BtcConstant.WS_REPAYMENT_URI,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public void contractRepayment(@RequestBody Map<String, Object> data) {
		if(BlockBaseHandler.currentMode.equals(BtcConstant.MODE_UPDATE)) {
			
			return;
		}
		String ip = "";
		try {
			
			if(data == null) {
				return;
			}
			String hostAddress = IpUtils.getRemoteIpAddr();
			ip = data.get("ip") != null? data.get("ip").toString():"";
			String tradeInfo = data.get("tradeInfo") !=null ?  data.get("tradeInfo").toString() : "";
			if(StringUtils.isBlank(ip) || StringUtils.isBlank(tradeInfo)) {
				
				return;
			}
			RepaymentTradeBody tb = TradeBodyImpl.mapToRepaymentTradeBody((Map)data.get("tradeInfo"));
			if(tb == null) {
				
				return;
			}
			if(StringUtils.isBlank(tb.getUserAddress()) 
					|| StringUtils.isBlank(tb.getTbAddress()) 
					|| StringUtils.isBlank(tb.getTbPublicKey()) 
					|| StringUtils.isBlank(tb.getUserPublicKey()) 
					|| StringUtils.isBlank(tb.getTradeNo()) 
					|| tb.getUserSign() == null 
					|| tb.getGas() ==null 
					|| new BigDecimal(tb.getRepaymentAmount()).compareTo(BigDecimal.ZERO) <= 0 
					|| new BigDecimal(tb.getGas()).compareTo(new BigDecimal("0.0006")) < 0) {
				throw new TokException(ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE, ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE);
			}
			
			
			TradeBody tby= new TradeBody();
			tby.setTradeNo(tb.getTradeNo());
			tby.setPublicKey(tb.getTbPublicKey());
			tby.setFrom(tb.getTbAddress());
			tby.setGas(tb.getGas());
			
			String now=DataUtils.getCurrentTime();
			
			checkTime(now, tb.getRepaymentTime());
			
			checkExist(tby);
			
			TradeUtils.validateTradeNo(tb);
			
			TradeUtils.validateAddress(tb);		
			TradeUtils.validateTbAddress(tb);
			
			TradeUtils.validateSign(tb);
			
			TradeUtils.validateGas(tb);
			
			genPending(tb);
			
			TradeUtils.sends(tb);
			
			FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_ADD);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			
			if(StringUtils.isNotBlank(ip)) {
				
				FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_LESS);
			}
		}
		
	}
	
	
	
	

	
	@PostMapping(value=BtcConstant.WS_STORAGESTATUS_URI,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public void contractTradeStatus(@RequestBody Map<String, Object> data) {
		if(BlockBaseHandler.currentMode.equals(BtcConstant.MODE_UPDATE)) {
			
			return;
		}
		String ip = "";
		try {
			
			if(data == null) {
				return;
			}
			String hostAddress = IpUtils.getRemoteIpAddr();
			ip = data.get("ip") != null? data.get("ip").toString():"";
			String tradeInfo = data.get("tradeInfo") !=null ?  data.get("tradeInfo").toString() : "";
			if(StringUtils.isBlank(ip) || StringUtils.isBlank(tradeInfo)) {
				
				return;
			}
			TradeStatusTradeBody tb = TradeBodyImpl.mapToTradeStatusTradeBody((Map)data.get("tradeInfo"));
			if(tb == null) {
				
				return;
			}
			if(StringUtils.isBlank(tb.getTradeNo()) 
			   || StringUtils.isBlank(tb.getStatus())) {
				throw new TokException(ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE, ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE);
			}
			
			
			TradeBody tby= new TradeBody();
			tby.setTradeNo(tb.getTradeNo());
		
		
			
			String now=DataUtils.getCurrentTime();
			
			checkTime(now, tb.getTradeTime());
			
			checkExist(tby);
			
			
			
		
			
		
		
			
			genPending(tb);
			
			TradeUtils.sends(tb);
			
			FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_ADD);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			
			if(StringUtils.isNotBlank(ip)) {
				
				FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_LESS);
			}
		}
		
	}
	
	@PostMapping(value=BtcConstant.WS_CREATE_INTELLIGENCECONTRACT,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public void createIn(@RequestBody Map<String, Object> data) {
		if(BlockBaseHandler.currentMode.equals(BtcConstant.MODE_UPDATE)) {
			
			return;
		}
		System.out.println(new Gson().toJson(data));
		String ip = "";
		try {
			
			if(data == null) {
				return;
			}
			String hostAddress = IpUtils.getRemoteIpAddr();
			ip = data.get("ip") != null? data.get("ip").toString():"";
			String tradeInfo = data.get("tradeInfo") !=null ?  data.get("tradeInfo").toString() : "";
			if(StringUtils.isBlank(ip) || StringUtils.isBlank(tradeInfo)) {
				
				return;
			}
			CreateIntelligenceContractTradeBody cictb = TradeBodyImpl.mapToCreateIntelligenceContractTradeBody((Map)data.get("tradeInfo"));
			if(cictb == null) {
				
				return;
			}
			if(StringUtils.isBlank(cictb.getAddress())
					|| StringUtils.isBlank(cictb.getPayAddress())
					|| StringUtils.isBlank(cictb.getContractName())
					|| StringUtils.isBlank(cictb.getContractCodeHash())
					|| StringUtils.isBlank(cictb.getContractCode())
					|| StringUtils.isBlank(cictb.getContractAddress())
					|| StringUtils.isBlank(cictb.getIncomeAddress())
					|| StringUtils.isBlank(cictb.getContractCode())
					|| StringUtils.isBlank(cictb.getTradeNo())
					|| StringUtils.isBlank(cictb.getAmount())
					|| StringUtils.isBlank(cictb.getGas())
					|| StringUtils.isBlank(cictb.getInitContractAmount())
					|| StringUtils.isBlank(cictb.getTradeNo())
					|| StringUtils.isBlank(cictb.getSign())
					|| StringUtils.isBlank(cictb.getTradeType())
					|| new BigDecimal(cictb.getAmount()).compareTo(BigDecimal.ZERO) <= 0
					|| new BigDecimal(cictb.getAmount()).compareTo(BtcConstant.CREATE_INTEL_CONTRACT_COST) !=0
					|| BtcConstant.CONTRACT_NAME_MIN_LENGTH > cictb.getContractName().trim().length()
					|| BtcConstant.CONTRACT_NAME_MAX_LENGTH < cictb.getContractName().trim().length()
					|| !cictb.getContractAddress().startsWith(BtcConstant.CONTRACT_ADDRESS_PRE)
					|| !cictb.getContractAddress().endsWith(cictb.getContractName())
					||  new BigDecimal(cictb.getGas()).compareTo(BtcConstant.MIN_GAS) < 0 
					|| cictb.getPayAddress().length()!=BtcConstant.TOKEN_ADDRESS_LENGTH
					|| cictb.getAddress().length()!=BtcConstant.TOKEN_ADDRESS_LENGTH
					|| !cictb.getContractAddress().equals(DataUtils.genContractAddress(cictb.getAddress(), cictb.getContractName()))) {
				throw new TokException(ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE, ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE);
			}
			
			TradeBody tby= new TradeBody();
			tby.setTradeNo(cictb.getTradeNo());
			tby.setPublicKey(cictb.getPayPublicKey());
			tby.setFrom(cictb.getPayAddress());
			tby.setGas(cictb.getGas());
			
			String now=DataUtils.getCurrentTime();
			checkTime(now, cictb.getTradeTime());
			
			checkExist(tby);
			
			TradeUtils.validateTradeNo(cictb);
			
			TradeUtils.validateAddress(cictb);
			
			TradeUtils.validateSign(cictb);
			
			TradeUtils.validateGas(cictb);
			
			genPending(cictb);
			
			TradeUtils.sends(cictb);
			
			FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_ADD);
			
			
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			
			if(StringUtils.isNotBlank(ip)) {
				
				FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_LESS);
			}
		}
		
	}
	
	@PostMapping(value=BtcConstant.WS_INTELLIGENCECONTRACT_METHOD, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public void inmethod(@RequestBody Map<String, Object> data) {
		if(BlockBaseHandler.currentMode.equals(BtcConstant.MODE_UPDATE)) {
			
			return;
		}
		String ip = "";
		try {
			
			if(data == null) {
				return;
			}
			String hostAddress = IpUtils.getRemoteIpAddr();
			ip = data.get("ip") != null? data.get("ip").toString():"";
			String tradeInfo = data.get("tradeInfo") !=null ?  data.get("tradeInfo").toString() : "";
			if(StringUtils.isBlank(ip) || StringUtils.isBlank(tradeInfo)) {
				
				return;
			}
			IntelligenceContractMethodTradeBody icmtb = TradeBodyImpl.mapToIntelligenceContractMethodTradeBody((Map)data.get("tradeInfo"));
			if(icmtb == null) {
				
				return;
			}
			
			List<MethodParams> mps = new ArrayList<MethodParams>();
			for(int i=0;i< icmtb.getMethodParams().size();i++) {
				Map m = (Map) icmtb.getMethodParams().get(i);
				MethodParams mp = new MethodParams();
				mp.setType(m.get("type").toString());
				mp.setValue(m.get("value").toString());
				mps.add(mp);
			}
			icmtb.setMethodParams(mps);
			if(StringUtils.isBlank(icmtb.getAddress())
					|| StringUtils.isBlank(icmtb.getContractName())
					|| StringUtils.isBlank(icmtb.getGas())
					|| StringUtils.isBlank(icmtb.getMethod())
					|| StringUtils.isBlank(icmtb.getPublicKey())
					|| StringUtils.isBlank(icmtb.getSign())
					|| StringUtils.isBlank(icmtb.getTradeNo())
					|| StringUtils.isBlank(icmtb.getTradeType())
					|| BtcConstant.CONTRACT_NAME_MIN_LENGTH > icmtb.getContractName().trim().length()
					|| BtcConstant.CONTRACT_NAME_MAX_LENGTH < icmtb.getContractName().trim().length()
					|| new BigDecimal(icmtb.getGas()).compareTo(BtcConstant.MIN_GAS) < 0 
					|| icmtb.getAddress().length()!=BtcConstant.TOKEN_ADDRESS_LENGTH) {
				throw new TokException(ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE, ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE);
			}
			
			TradeBody tby= new TradeBody();
			tby.setTradeNo(icmtb.getTradeNo());
			tby.setPublicKey(icmtb.getPublicKey());
			tby.setFrom(icmtb.getAddress());
			tby.setGas(icmtb.getGas());
			
			String now=DataUtils.getCurrentTime();
			checkTime(now, icmtb.getTradeTime());
			
			checkExist(tby);
			
			TradeUtils.validateTradeNo(icmtb);
			
			TradeUtils.validateAddress(icmtb);
			
			TradeUtils.validateSign(icmtb);
			
			TradeUtils.validateGas(icmtb);
			
			genPending(icmtb);
			
			TradeUtils.sends(icmtb);
			
			FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_ADD);
			
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			
			if(StringUtils.isNotBlank(ip)) {
				
				FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_LESS);
			}
		}
		
	}
	@Deprecated
	@PostMapping(value=BtcConstant.GETBLOCK,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public void getBlock(@RequestBody Map<String, Object> data) {
		
		try {
	
			
			if(data == null) {
				return;
			}
			

			String randomNumber = data.get("randomNumber") !=null ?  data.get("randomNumber").toString() : "";
			
			Block tb  = Mining.mi.getBdl().getBlock();

			if(tb == null) {
				
				return;
			}
			
			String blockIndex = data.get("blockIndex") !=null ?  data.get("blockIndex").toString() : "";

			if(!blockIndex.equals(tb.getBlockIndex())) {
				return;
			}
			
			tb.setRandomNumber(randomNumber);
			tb.setEndHash(DataUtils.getEndHash(Mining.mi.getBlockHash(), tb.getPath(), tb.getCreateTime(), tb.getRandomNumber()));
	

			if(MiningThread.mining) {
				String outStr = Mining.blockHash + tb.genString();
				String outHash = EncryptUtil.encryptSHA256(outStr);
				Dictionary dic = InitUtils.intiDifficulty();
	

				if(outHash.startsWith(dic.getValue())) {
		

					
					
					Block blockExist =  BlockServiceImpl.queryBlockByBlockIndexOnMingChain(tb.getBlockIndex());
					if(blockExist == null) {
						 MiningInfo mi =Mining.mi;
						TradeBodyPool tbp=mi.getTbp();
						
						
						MiningThread.deletePendings0(tbp);
						
						DicServiceImpl.updateDicBlockIndex(tb.getBlockIndex());
						DicServiceImpl.updateDicMainBockIndex(tb.getBlockIndex());
						
						updateBlockAllAddressList(mi.getBalMap(), 1);
						
						BlockServiceImpl.checkBlockTable(tb.getBlockIndex());
						BlockServiceImpl.save(mi.getBdl().getBlock());
						BlockServiceImpl.saveBlockFile(mi.getBdl());
						BlockServiceImpl.saveContractBlockFile(mi.getBdl());
						
						BlockServiceImpl.saveBlockInFile(mi.getBdl().getBlock(), mi.getBalMap());

						
						BlockBaseHandler.updateAllBlockCounter2(tbp, tb);
						
						
						MiningThread.miningBlock = tb;
						
					}else {
						MiningThread.miningBlock = blockExist;
		

					}
					
					
					MiningThread.miningInfo = null;
					Mining.block=null;
					Mining.mi=null;
					Mining.blockHash="";
					
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					MiningThread mt = new MiningThread();
					mt.start();
					
	

				}
			}
			
		
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		
		}
		
	}
	
	@PostMapping(value=BtcConstant.WS_CONTRACTCONVERT,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public void contractConvertTradeBody(@RequestBody Map<String, Object> data) {
		if(BlockBaseHandler.currentMode.equals(BtcConstant.MODE_UPDATE)) {
			
			return;
		}
		String ip = "";
		try {
			
			if(data == null) {
				return;
			}
			String hostAddress = IpUtils.getRemoteIpAddr();
			ip = data.get("ip") != null? data.get("ip").toString():"";
			String tradeInfo = data.get("tradeInfo") !=null ?  data.get("tradeInfo").toString() : "";
			if(StringUtils.isBlank(ip) || StringUtils.isBlank(tradeInfo)) {
				
				return;
			}
			ContractConvertTradeBody tb = TradeBodyImpl.mapToContractConvertTradeBody((Map)data.get("tradeInfo"));
			if(tb == null) {
				
				return;
			}
			if(StringUtils.isBlank(tb.getUserAddress01()) 
					|| StringUtils.isBlank(tb.getUserAddress02()) 
					|| StringUtils.isBlank(tb.getUserPublicKey01()) 
					|| StringUtils.isBlank(tb.getUserPublicKey01()) 
					|| StringUtils.isBlank(tb.getTradeNo()) 
					|| tb.getSign01() == null 
					|| tb.getSign02() == null 
					|| new BigDecimal(tb.getAmount01()).compareTo(BigDecimal.ZERO) <= 0 
					|| new BigDecimal(tb.getAmount02()).compareTo(BigDecimal.ZERO) <= 0 
					|| new BigDecimal(tb.getGas()).compareTo(new BigDecimal("0.0006")) < 0 ) {
				throw new TokException(ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE, ErrorInfo.TRADE_PARAMS_NOT_CORRECT_CODE);
			}
			
			
			TradeBody tby= new TradeBody();
			tby.setTradeNo(tb.getTradeNo());
			String now=DataUtils.getCurrentTime();
			

			
			checkExist(tby);
			
			TradeUtils.validateTradeNo(tb);
			
			TradeUtils.validateTbAddress01(tb);		
			TradeUtils.validateTbAddress02(tb);
			
			TradeUtils.validateSign01(tb);
			TradeUtils.validateSign02(tb);
			
			TradeUtils.validateGas(tb);
		
			
			genPending(tb);
			
			TradeUtils.sends(tb);
		
			
			FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_ADD);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			
			if(StringUtils.isNotBlank(ip)) {
				
				FriendServiceImpl.updateFriendliness2(ip, BtcConstant.FRIENDLINESS_LESS);
			}
		}
		
	}
	private void genPending(TradeBody tb) throws TokException {
		Pending pending = new Pending();
		pending.setOrderNo(tb.getTradeNo());
		pending.setCreateTime(DataUtils.getCurrentTime());
		pending.setGas(tb.getGas());
		pending.setTradeBody(new Gson().toJson(tb));
		pending.setCreateTime(tb.getTradeTime());
		pending.setTradeType(tb.getTradeType());
		PendingServiceImpl.save(pending);
	}
	
	private void genPending(LoanByFBDTTradeBody tb) throws TokException {
		Pending pending = new Pending();
		pending.setOrderNo(tb.getTradeNo());
		pending.setCreateTime(DataUtils.getCurrentTime());
		pending.setGas(tb.getGas());
		pending.setTradeBody(new Gson().toJson(tb));
		pending.setCreateTime(tb.getStarTime());
		pending.setTradeType(tb.getTradeType());
		PendingServiceImpl.save(pending);
	}
	
	private void genPending(LoanByTOKTradeBody tb) throws TokException {
		Pending pending = new Pending();
		pending.setOrderNo(tb.getTradeNo());
		pending.setCreateTime(DataUtils.getCurrentTime());
		pending.setGas(tb.getGas());
		pending.setTradeBody(new Gson().toJson(tb));
		pending.setCreateTime(tb.getLoanTime());
		pending.setTradeType(tb.getTradeType());
		PendingServiceImpl.save(pending);
	}
	
	
	private void genPending(TradeStatusTradeBody tb) throws TokException {
		Pending pending = new Pending();
		pending.setOrderNo(tb.getTradeNo());
		pending.setCreateTime(DataUtils.getCurrentTime());
		pending.setTradeBody(new Gson().toJson(tb));
		pending.setCreateTime(tb.getStorageStatusTime());
		pending.setTradeType(tb.getTradeType());
		PendingServiceImpl.save(pending);
	}
	
	
	private void genPending(RepaymentTradeBody tb) throws TokException {
		Pending pending = new Pending();
		pending.setOrderNo(tb.getTradeNo());
		pending.setCreateTime(DataUtils.getCurrentTime());
		pending.setGas(tb.getGas());
		pending.setTradeBody(new Gson().toJson(tb));
		pending.setCreateTime(tb.getRepaymentTime());
		pending.setTradeType(tb.getTradeType());
		PendingServiceImpl.save(pending);
	}
	
	
	
	private void genPending(ContractToContractTradeBody tb) throws TokException {
		Pending pending = new Pending();
		pending.setOrderNo(tb.getTradeNo());
		pending.setCreateTime(DataUtils.getCurrentTime());
		pending.setGas(tb.getGas());
		pending.setTradeBody(new Gson().toJson(tb));
		pending.setCreateTime(tb.getTradeTime());
		pending.setTradeType(tb.getTradeType());
		PendingServiceImpl.save(pending);
	}
	
	private void genPending(ContractToContractAddressTradebody tb) throws TokException {
		Pending pending = new Pending();
		pending.setOrderNo(tb.getTradeNo());
		pending.setCreateTime(DataUtils.getCurrentTime());
		pending.setGas(tb.getGas());
		pending.setTradeBody(new Gson().toJson(tb));
		pending.setCreateTime(tb.getTradeTime());
		pending.setTradeType(tb.getTradeType());
		PendingServiceImpl.save(pending);
	}
	

	private void genPending(CreateContractTradeBody tb) throws TokException {
		Pending pending = new Pending();
		pending.setOrderNo(tb.getTradeNo());
		pending.setCreateTime(DataUtils.getCurrentTime());
		pending.setGas(tb.getGas());
		pending.setTradeBody(new Gson().toJson(tb));
		pending.setCreateTime(tb.getTradeTime());
		pending.setTradeType(tb.getTradeType());
		PendingServiceImpl.save(pending);
	}
	
	private void genPending(ContractWithdrawTradebody tb) throws TokException {
		Pending pending = new Pending();
		pending.setOrderNo(tb.getTradeNo());
		pending.setCreateTime(DataUtils.getCurrentTime());
		pending.setGas(tb.getGas());
		pending.setTradeBody(new Gson().toJson(tb));
		pending.setCreateTime(tb.getTradeTime());
		pending.setTradeType(tb.getTradeType());
		PendingServiceImpl.save(pending);
	}
	private void genPending(TokToContractAddressTradebody tb) throws TokException {
		Pending pending = new Pending();
		pending.setOrderNo(tb.getTradeNo());
		pending.setCreateTime(DataUtils.getCurrentTime());
		pending.setGas(tb.getGas());
		pending.setTradeBody(new Gson().toJson(tb));
		pending.setCreateTime(tb.getTradeTime());
		pending.setTradeType(tb.getTradeType());
		PendingServiceImpl.save(pending);
	}
	
	
	
	private void genPending(StorageTradeBody tb) throws TokException {
		Pending pending = new Pending();
		pending.setOrderNo(tb.getTradeNo());
		pending.setCreateTime(DataUtils.getCurrentTime());
		pending.setGas(tb.getGas());
		pending.setTradeBody(new Gson().toJson(tb));
		pending.setCreateTime(tb.getStarTime());
		pending.setTradeType(tb.getTradeType());
		PendingServiceImpl.save(pending);
	}
	private void genPending(ContractConvertTradeBody tb) throws TokException {
		Pending pending = new Pending();
		pending.setOrderNo(tb.getTradeNo());
		pending.setCreateTime(DataUtils.getCurrentTime());
		pending.setGas(tb.getGas());
		pending.setTradeBody(new Gson().toJson(tb));
		pending.setCreateTime(tb.getTradeTime02());
		pending.setTradeType(tb.getTradeType());
		PendingServiceImpl.save(pending);
	}
	private void genPending(CreateIntelligenceContractTradeBody tb) throws TokException {
		Pending pending = new Pending();
		pending.setOrderNo(tb.getTradeNo());
		pending.setCreateTime(DataUtils.getCurrentTime());
		pending.setGas(tb.getGas());
		pending.setTradeBody(new Gson().toJson(tb));
		pending.setCreateTime(tb.getTradeTime());
		pending.setTradeType(tb.getTradeType());
		PendingServiceImpl.save(pending);
	}
	
	private void genPending(IntelligenceContractMethodTradeBody tb) throws TokException {
		Pending pending = new Pending();
		pending.setOrderNo(tb.getTradeNo());
		pending.setCreateTime(DataUtils.getCurrentTime());
		pending.setGas(tb.getGas());
		pending.setTradeBody(new Gson().toJson(tb));
		pending.setCreateTime(tb.getTradeTime());
		pending.setTradeType(tb.getTradeType());
		PendingServiceImpl.save(pending);
	}
	
	private void checkExist(TradeBody tb) throws TokException {
		Pending p = PendingServiceImpl.queryPendingsByTradeNo(tb.getTradeNo());
		if(p !=null) {
			
			throw new TokException(ErrorInfo.TRADE_DB_PENDING_FAILTURE_EXIST_CODE, ErrorInfo.TRADE_DB_PENDING_FAILTURE_EXIST_CODE_MSG);
		}
	}
	@Deprecated
	protected void updateBlockAllAddressList(Map<String , BlockAddressList> balMap, int type) {
		
		
		for(Entry<String, BlockAddressList> entry : balMap.entrySet()){
			String key = entry.getKey();
			BlockAddressList bal = entry.getValue();
			
			
			BlockAllAddressList  baal0 = CacheUtils.getBlockAllAddressList(key);
			
			
			if(baal0 != null) {
				
				
				if(type == 1) {
					BigDecimal newExpenses = new BigDecimal(baal0.getExpenses()).add(new BigDecimal(bal.getExpenses()));
					BigDecimal income = new BigDecimal(baal0.getIncome()).add(new BigDecimal(bal.getIncome()));
					
					BigDecimal left = new BigDecimal(bal.getLeft());
					baal0.setExpenses(DataUtils.getAmountString(newExpenses));
					baal0.setIncome(DataUtils.getAmountString(income));
					baal0.setLeft(DataUtils.getAmountString(left));
					baal0.getTimeAndAmounts().addAll(bal.getTimeAndAmounts());
				}else {
					BigDecimal newExpenses = new BigDecimal(baal0.getExpenses()).add(new BigDecimal(bal.getExpenses()).negate());
					BigDecimal income = new BigDecimal(baal0.getIncome()).add(new BigDecimal(bal.getIncome()).negate());
					
					BigDecimal left = new BigDecimal(bal.getLeft());
					baal0.setExpenses(DataUtils.getAmountString(newExpenses));
					baal0.setIncome(DataUtils.getAmountString(income));
					baal0.setLeft(DataUtils.getAmountString(left));
					baal0.getTimeAndAmounts().removeAll(bal.getTimeAndAmounts());
				}
				
				CacheUtils.putBlockAllAddressList(key, baal0);
				
			}else {
				
				BlockAllAddressList baal = new BlockAllAddressList();
				baal.setAddress(bal.getAddress());
				baal.setContractNumber(bal.getContractNumber());
				baal.setExpenses(bal.getExpenses());
				baal.setIncome(bal.getIncome());
				baal.setLeft(bal.getLeft());
				baal.getTimeAndAmounts().addAll(bal.getTimeAndAmounts());
				
				CacheUtils.putBlockAllAddressList(key, baal);
				
			}
		}
		CacheUtils.flushBlockAllAddressList();
	}
	
	private void checkTime(String date1,String date2) throws TokException {
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(BtcConstant.DATE_FORMAT);
		try {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MINUTE, -1);
			Date d11 = c.getTime();
			c.add(Calendar.MINUTE, 2);
			Date d12 = c.getTime();
			Date d2=simpleDateFormat.parse(date2);
			if(d2.before(d11) || d2.after(d12)) {
				throw new TokException(ErrorInfo.TRADE_TIME_IS_NOT_LEGAL, ErrorInfo.TTRADE_TIME_IS_NOT_LEGAL_MSG);
			}
			
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		
		
	
	}
}
