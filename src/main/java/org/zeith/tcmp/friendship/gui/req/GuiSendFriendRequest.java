package org.zeith.tcmp.friendship.gui.req;

import com.mojang.authlib.GameProfile;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.*;
import org.zeith.hammeranims.api.McUtil;
import org.zeith.tcmp.friendship.FriendshipDatabase;
import org.zeith.tcmp.friendship.gui.GuiOverScreen;
import org.zeith.tcmp.friendship.net.*;
import org.zeith.tcmp.friendship.net.client.FriendshipPromise;
import org.zeith.tcmp.proxy.ClientProxy;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public class GuiSendFriendRequest
		extends GuiOverScreen
{
	public GuiTextField field;
	
	public GuiSendFriendRequest(GuiScreen parent)
	{
		super(parent);
	}
	
	@Override
	public void initGui()
	{
		int totHeight = (20 + 2) * 3;
		
		int y = (height - totHeight) / 2;
		
		String pt = field != null ? field.getText() : "";
		field = new GuiTextField(
				0,
				fontRenderer,
				width / 2 - 100, y,
				200, 20
		);
		field.setMaxStringLength(64);
		field.setText(pt);
		
		y += 24;
		
		addButton(new GuiButton(1, width / 2 - 100, y, 200, 20, I18n.format("gui.tcmp:send_request")));
		
		y += 22;
		addButton(new GuiButton(0, width / 2 - 100, y, 200, 20, I18n.format("gui.back")));
		
		super.initGui();
	}
	
	@Override
	protected void actionPerformed(GuiButton button)
			throws IOException
	{
		if(button.id == 0) close();
		if(button.id == 1)
		{
			sendFriendRequest(field.getText());
			close();
		}
		
		super.actionPerformed(button);
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode)
			throws IOException
	{
		if(field.textboxKeyTyped(typedChar, keyCode)) return;
		super.keyTyped(typedChar, keyCode);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
			throws IOException
	{
		if(field.mouseClicked(mouseX, mouseY, mouseButton)) return;
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void drawOverlay(int mouseX, int mouseY, float partialTicks)
	{
		field.drawTextBox();
	}
	
	public static void sendFriendRequest(String targetId)
	{
		CompletableFuture.runAsync(() ->
		{
			OnlinePerson person = FriendshipConstants.list().get(targetId);
			
			Minecraft mc = Minecraft.getMinecraft();
			if(person == null)
			{
				mc.addScheduledTask(() ->
				{
					ITextComponent tc = new TextComponentTranslation("toast.tcmp.inactive_code");
					GuiToast gui = mc.getToastGui();
					gui.add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, tc, new TextComponentString("")));
				});
				return;
			}
			
			FriendshipDatabase db = ClientProxy.getDatabase();
			
			FriendshipRequest request = new FriendshipRequest(mc.getSession().getProfile(), db.authSignature());
			
			Duration timeout = Duration.ofMinutes(5L);
			
			CompletableFuture<FriendshipApproval> approval = FriendshipPromise.friendRequest(ClientProxy.getFriendshipServer().getApi(), db, request, person, timeout)
					.thenApply(a ->
					{
						if(a != null) db.storeApproval(a);
						mc.addScheduledTask(() ->
						{
							ITextComponent tc = new TextComponentTranslation(a == null ? "toast.tcmp.request_declined" : "toast.tcmp.request_accepted");
							GuiToast gui = mc.getToastGui();
							gui.add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, tc, new TextComponentString(person.getUsername())));
						});
						return a;
					});
			
			GameProfile profile = new GameProfile(null, person.getUsername());
			val now = Instant.now();
			GuiFriendRequests.friendRequest(new OutgoingFriendRequest(profile, approval, now, now.plus(timeout)), person.getUsername());
		}, McUtil.backgroundExecutor());
	}
}