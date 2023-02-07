package com.statter.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import com.statter.cache.TaskManagerCache;
import com.statter.core.common.SpringContextUtils;
import com.statter.data.TaskManager;
import com.statter.gateway.PoolTaskInfo;
import com.statter.impl.TaskManagerServiceImpl;

@Component
public class ApplicationCloseListener implements ApplicationListener<ContextClosedEvent> {


	@Autowired
	private TaskManagerCache taskManagerCache;

	@Autowired
	private TaskManagerServiceImpl taskManagerService;
	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		
	
		TaskManager tm=taskManagerCache.getTaskManager();
		tm.setStatus(PoolTaskInfo.STATUS_STOP+"");
		taskManagerCache.putTaskManagerCache(tm);
		System.out.println(PoolTaskInfo.STATUS_STOP);
		taskManagerService.setStatus(PoolTaskInfo.STATUS_STOP+"");

	}

}
