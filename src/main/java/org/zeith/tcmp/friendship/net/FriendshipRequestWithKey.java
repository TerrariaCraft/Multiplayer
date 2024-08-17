package org.zeith.tcmp.friendship.net;

import lombok.Builder;
import lombok.Value;

import java.security.PublicKey;

@Value
@Builder(toBuilder = true)
public class FriendshipRequestWithKey
{
	PublicKey key;
	FriendshipRequest request;
}