package org.zeith.tcmp.friendship.gui.req;

import com.mojang.authlib.GameProfile;
import com.zeitheron.hammercore.utils.color.ColorHelper;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.text.*;
import org.lwjgl.input.Keyboard;
import org.zeith.tcmp.friendship.gui.McAuth;
import org.zeith.tcmp.friendship.gui.lst.GuiFriendList;
import org.zeith.tcmp.proxy.ClientProxy;
import org.zeith.terraria.client.gui.api.GuiBaseMainBG;
import org.zeith.terraria.init.SoundsTC;

import java.io.IOException;
import java.util.*;

public class GuiFriendRequests
		extends GuiBaseMainBG
{
	public static List<BaseFriendRequest> INCOMING_REQUESTS = new ArrayList<>();
	
	private final GuiFriendList parent;
	private RequestList requestList;
	
	private GameProfile profile;
	
	public GuiFriendRequests(GuiFriendList parent)
	{
		setFrom(parent);
		this.parent = parent;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		profile = mc.getSession().getProfile();
		profile = McAuth.updateProfile(profile);
		
		INCOMING_REQUESTS.removeIf(BaseFriendRequest::isExpired);
		
		requestList = new RequestList(this.mc, width - width / 3 - 64, this.height, 21, height - 24);
		requestList.setSlotXBoundsFromLeft(width / 3 + 32);
		requestList.registerScrollButtons(7, 8);
		
		addButton(new GuiButton(0, width / 2 - 50 + 110, height - 22, 100, 20, I18n.format("gui.back")));
		addButton(new GuiButton(2, width / 2 - 50, height - 22, 100, 20, I18n.format("gui.tcmp:send_request")));
		
		int w = 32;
		int pad = 12;
		int uvs = width / 3 - 8 - pad * 2;
		addButton(new GuiButton(1, w + pad, height - 24 - 22, uvs, 20, I18n.format("gui.tcmp:copy_friend_code")));
	}
	
	@Override
	public void updateScreen()
	{
		super.updateScreen();
		
		INCOMING_REQUESTS.removeIf(BaseFriendRequest::isExpired);
		
		parent.ticksExisted++;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
		
		GlStateManager.disableAlpha();
		this.draw25D(mouseX, mouseY, partialTicks);
		GlStateManager.enableAlpha();
		
		int color = ColorHelper.packARGB(Math.min((parent.ticksExisted + partialTicks) / 20F, 1F) * 0.35F, 0F, 0F, 0F);
		int color2 = ColorHelper.packARGB(Math.min((parent.ticksExisted + partialTicks) / 20F, 1F) * 0.5F, 0F, 0F, 0F);
		
		this.drawGradientRect(0, 0, this.width, this.height, color, color);
		
		int w = 32;
		this.drawGradientRect(w, 21, width / 3 + 24, this.height - 24, color, color);
		
		this.drawGradientRect(width / 3 + w, 21, this.width - w, this.height - 24, color, color);
		this.drawGradientRect(w * 4, 2, this.width - w * 4, 16, color2, color2);
		this.drawGradientRect(w * 4, 16, this.width - w * 4, 21, color2, 0xFF000000);
		
		drawCenteredString(fontRenderer, I18n.format("gui.tcmp:friend_requests"), width / 2, (21 - fontRenderer.FONT_HEIGHT) / 2, 0xFFFFFFFF);
		
		int pad = 32;
		int uvs = width / 3 - 8 - pad * 2;
		
		SkinHeadRenderer.renderSkin(profile,
				mc.gameSettings.getModelParts().contains(EnumPlayerModelParts.HAT),
				w + pad, 21 + pad, uvs, uvs
		);
		
		String key = ClientProxy.getDatabase().getMyKey();
		int kh = fontRenderer.getWordWrappedHeight(key, uvs);
		fontRenderer.drawSplitString(key, w + pad, Math.max(25 + pad + uvs, height - 24 * 3 - kh), uvs, 0xFFFFFFFF);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		requestList.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
			throws IOException
	{
		for(int i = 0; i < INCOMING_REQUESTS.size(); i++)
		{
			val r = INCOMING_REQUESTS.get(i);
			if(r.hoveredButton >= 0)
			{
				r.clickButton(r.hoveredButton, mouseButton);
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundsTC.MENU_TICK.sound, 1F));
				return;
			}
		}
		requestList.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public void handleMouseInput()
			throws IOException
	{
		requestList.handleMouseInput();
		super.handleMouseInput();
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode)
	{
		if(keyCode == Keyboard.KEY_ESCAPE) close();
	}
	
	@Override
	protected void actionPerformed(GuiButton button)
			throws IOException
	{
		if(button.id == 0) close();
		if(button.id == 1) setClipboardString(ClientProxy.getDatabase().getMyKey());
		if(button.id == 2) mc.displayGuiScreen(new GuiSendFriendRequest(this));
		super.actionPerformed(button);
	}
	
	public void close()
	{
		setTo(this.parent);
		this.mc.displayGuiScreen(this.parent);
		if(this.mc.currentScreen == null) this.mc.setIngameFocus();
	}
	
	public static void friendRequest(OutgoingFriendRequest request, String username)
	{
		val mc = Minecraft.getMinecraft();
		mc.addScheduledTask(() ->
		{
			INCOMING_REQUESTS.add(request);
			ITextComponent tc = new TextComponentTranslation("toast.tcmp.request_sent");
			GuiToast gui = mc.getToastGui();
			gui.add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, tc, new TextComponentString(username)));
		});
	}
	
	public static void friendRequest(IncomingFriendRequest request)
	{
		val key = request.getRequest().getKey();
		INCOMING_REQUESTS.removeIf(f ->
		{
			if(!(f instanceof IncomingFriendRequest)) return false;
			IncomingFriendRequest f2 = (IncomingFriendRequest) f;
			if(key.equals(f2.getRequest().getKey()))
			{
				// Ignore previous request, basically refreshing the same request!
				f2.getApprovalSignal().complete(Optional.empty());
				return true;
			}
			return false;
		});
		
		INCOMING_REQUESTS.add(request);
		
		ITextComponent tc = new TextComponentTranslation("toast.tcmp.incoming_request");
		ITextComponent sub = new TextComponentString(request.getRequest().getRequest().profile.getName());
		GuiToast gui = Minecraft.getMinecraft().getToastGui();
		gui.add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, tc, sub));
	}
	
	public class RequestList
			extends GuiListExtended
	{
		public RequestList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn)
		{
			super(mcIn, widthIn, heightIn, topIn, bottomIn, 32);
		}
		
		@Override
		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
		{
			super.elementClicked(slotIndex, isDoubleClick, mouseX, mouseY);
		}
		
		@Override
		public IGuiListEntry getListEntry(int index)
		{
			return INCOMING_REQUESTS.get(index);
		}
		
		@Override
		protected int getSize()
		{
			return INCOMING_REQUESTS.size();
		}
		
		@Override
		protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha)
		{
		}
		
		@Override
		protected void drawContainerBackground(Tessellator tessellator)
		{
		}
	}
}
