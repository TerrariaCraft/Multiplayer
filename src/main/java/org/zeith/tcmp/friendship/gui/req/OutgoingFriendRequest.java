package org.zeith.tcmp.friendship.gui.req;

import com.mojang.authlib.GameProfile;
import lombok.*;
import net.minecraft.client.resources.I18n;
import org.zeith.tcmp.friendship.net.FriendshipApproval;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Getter
public class OutgoingFriendRequest
		extends BaseFriendRequest
{
	private final CompletableFuture<FriendshipApproval> approval;
	private final Instant requestTimestamp;
	private final Instant expiryTimestamp;
	
	public OutgoingFriendRequest(GameProfile profile, CompletableFuture<FriendshipApproval> approval, Instant requestTimestamp, Instant expiryTimestamp)
	{
		super(profile);
		this.approval = approval;
		this.requestTimestamp = requestTimestamp;
		this.expiryTimestamp = expiryTimestamp;
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
				I18n.format("gui.cancel")
		};
	}
	
	@Override
	public void clickButton(int button, int mouseBtn)
	{
		if(button == 0 && mouseBtn == 0)
			approval.cancel(true);
	}
	
	@Override
	public long getMsTillExpiry()
	{
		val now = Instant.now().toEpochMilli();
		val exp = expiryTimestamp.toEpochMilli();
		if(now >= exp) return 0;
		return exp - now;
	}
	
	@Override
	public double expiryProgress()
	{
		val now = Instant.now();
		if(now.isAfter(expiryTimestamp)) return 1.0;
		if(requestTimestamp.isAfter(now)) return 0.0;
		long start = requestTimestamp.toEpochMilli();
		long progress = now.toEpochMilli() - start;
		long maxProgress = expiryTimestamp.toEpochMilli() - start;
		return progress / (double) maxProgress;
	}
	
	@Override
	public boolean isExpired()
	{
		return Instant.now().isAfter(expiryTimestamp) || approval.isDone();
	}
}