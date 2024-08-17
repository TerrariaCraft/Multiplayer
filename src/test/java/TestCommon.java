import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.cloudflared.core.CloudflaredAPIFactory;
import org.zeith.cloudflared.core.api.*;
import org.zeith.cloudflared.core.exceptions.CloudflaredNotFoundException;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestCommon
{
	public static final CloudflaredAPI api;
	
	static
	{
		try
		{
			ExecutorService exec = Executors.newFixedThreadPool(16, r ->
			{
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			});
			api = CloudflaredAPI.create(CloudflaredAPIFactory.builder()
					.gameProxy(new IGameProxy()
					{
						List<IGameListener> listeners = new ArrayList<>();
						
						@Override
						public ExecutorService getBackgroundExecutor()
						{
							return exec;
						}
						
						@Override
						public void addListener(IGameListener listener)
						{
							listeners.add(listener);
						}
						
						@Override
						public void removeListener(IGameListener listener)
						{
							listeners.remove(listener);
						}
						
						@Override
						public void sendChatMessage(String message)
						{
							System.out.println(message);
						}
						
						@Override
						public void createToast(InfoLevel level, String title, String subtitle)
						{
							System.out.println(level + ": " + title + "\t\t" + subtitle);
						}
						
						@Override
						public List<IGameListener> getListeners()
						{
							return listeners;
						}
					})
					.build());
		} catch(CloudflaredNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
}