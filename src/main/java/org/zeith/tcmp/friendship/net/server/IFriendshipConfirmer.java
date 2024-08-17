package org.zeith.tcmp.friendship.net.server;

import org.zeith.tcmp.friendship.net.FriendshipApproval;
import org.zeith.tcmp.friendship.net.FriendshipRequestWithKey;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IFriendshipConfirmer
{
	CompletableFuture<Optional<FriendshipApproval>> confirm(FriendshipRequestWithKey request, Duration timeout);
}