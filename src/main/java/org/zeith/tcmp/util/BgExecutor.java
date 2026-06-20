package org.zeith.tcmp.util;

import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.log4j.Log4j2;
import org.zeith.hammeranims.api.McUtil;

import java.util.concurrent.ExecutorService;

@Log4j2
public class BgExecutor
{
	private static final ExecutorService executor;
	
	static
	{
		ExecutorService e;
		try
		{
			e = McUtil.backgroundExecutor();
			log.info("Using HammerAnims background executor.");
		} catch(Throwable t)
		{
			try
			{
				e = org.zeith.hammerlib.util.mcf.McUtil.backgroundExecutor();
				log.info("Using HammerLib background executor.");
			} catch(Throwable t2)
			{
				log.warn("Unable to find either HammerAnims or HammerLib McUtil.backgroundExecutor()! Using direct executor service.");
				e = MoreExecutors.newDirectExecutorService();
			}
		}
		executor = e;
	}
	
	public static ExecutorService bgExecutor()
	{
		return executor;
	}
}