package org.zeith.tcmp.friendship.gui.req;

import lombok.Getter;
import lombok.val;
import net.minecraft.client.resources.I18n;
import org.zeith.tcmp.friendship.net.FriendshipApproval;
import org.zeith.tcmp.friendship.net.FriendshipRequestWithKey;
import org.zeith.tcmp.proxy.ClientProxy;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Getter
public class IncomingFriendRequest
		extends BaseFriendRequest
{
	private final FriendshipRequestWithKey request;
	private final CompletableFuture<Optional<FriendshipApproval>> approvalSignal;
	private final Instant requestTimestamp;
	private final Instant expiryTimestamp;
	
	public IncomingFriendRequest(FriendshipRequestWithKey request, CompletableFuture<Optional<FriendshipApproval>> approvalSignal, Instant requestTimestamp, Instant expiryTimestamp)
	{
		super(request.getRequest().profile);
		this.request = request;
		this.approvalSignal = approvalSignal;
		this.requestTimestamp = requestTimestamp;
		this.expiryTimestamp = expiryTimestamp;
	}
	
	@Override
	public String getRequestType()
	{
		return I18n.format("toast.tcmp.incoming_request");
	}
	
	@Override
	public String[] getButtons()
	{
		return new String[] {
				I18n.format("gui.tcmp:accept"),
				I18n.format("gui.tcmp:decline")
		};
	}
	
	@Override
	public void clickButton(int button, int mouseBtn)
	{
		if(mouseBtn != 0) return;
		
		if(button == 0)
		{
			approvalSignal.complete(Optional.ofNullable(ClientProxy.getDatabase().acceptRequest(request)));
		} else if(button == 1)
		{
			approvalSignal.complete(Optional.empty());
		}
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
		return Instant.now().isAfter(expiryTimestamp) || approvalSignal.isDone() || approvalSignal.isCancelled();
	}
}