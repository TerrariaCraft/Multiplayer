package org.zeith.tcmp.friendship.gui.req;

import com.mojang.authlib.GameProfile;
import com.zeitheron.hammercore.client.utils.SkinUtils;
import com.zeitheron.hammercore.client.utils.UV;
import lombok.val;

public class SkinHeadRenderer
{
	public static void renderSkin(GameProfile profile, boolean hat, int x, int y, int width, int height)
	{
		if(profile == null) return;
		
		val st = SkinUtils.getSkinTexture(profile);
		
		UV u = new UV(st, 8 * 4, 8 * 4, 8 * 4, 8 * 4);
		u.render(x, y, width, height);
		if(hat)
		{
			u = new UV(st, 40 * 4, 8 * 4, 8 * 4, 8 * 4);
			u.render(x, y, width, height);
		}
	}
}