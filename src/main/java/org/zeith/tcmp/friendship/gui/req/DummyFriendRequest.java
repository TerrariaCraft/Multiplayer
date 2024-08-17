package org.zeith.tcmp.friendship.gui.req;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.resources.I18n;

import java.util.concurrent.TimeUnit;

public class DummyFriendRequest
		extends BaseFriendRequest
{
	public DummyFriendRequest(String username)
	{
		super(new GameProfile(null, username));
	}
	
	@Override
	public String getRequestType()
	{
		return I18n.format("toast.tcmp.outgoing_request");
	}
	
	@Override
	public String[] getButtons()
	{
		return new String[] {
				"A",
				"B"
		};
	}
	
	@Override
	public void clickButton(int button, int mouseBtn)
	{
		System.out.println(button);
	}
	
	@Override
	public long getMsTillExpiry()
	{
		return TimeUnit.SECONDS.toMillis(30L);
	}
	
	@Override
	public double expiryProgress()
	{
		return 0.75F;
	}
	
	@Override
	public boolean isExpired()
	{
		return false;
	}
}