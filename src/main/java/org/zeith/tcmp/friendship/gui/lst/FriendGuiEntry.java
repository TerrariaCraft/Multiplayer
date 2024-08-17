package org.zeith.tcmp.friendship.gui.lst;

import com.mojang.authlib.GameProfile;
import com.zeitheron.hammercore.client.utils.RenderUtil;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.zeith.tcmp.TCMultiplayer;
import org.zeith.tcmp.friendship.FriendEntry;
import org.zeith.tcmp.friendship.gui.McAuth;
import org.zeith.tcmp.friendship.gui.connect.GuiEstablishingConnection;
import org.zeith.tcmp.friendship.gui.req.SkinHeadRenderer;
import org.zeith.tcmp.friendship.net.OnlinePerson;
import org.zeith.tcmp.proxy.ClientProxy;
import org.zeith.terraria.client.util.BlinkingText;

public class FriendGuiEntry
		implements GuiListExtended.IGuiListEntry
{
	protected final int index;
	protected final FriendEntry entry;
	public OnlinePerson online;
	
	public int hoveredButton;
	public GameProfile profile;
	
	public FriendGuiEntry(int index, FriendEntry entry)
	{
		this.profile = entry.getProfile();
		this.index = index;
		this.entry = entry;
		if(this.profile != null) this.profile = McAuth.updateProfile(this.profile);
	}
	
	public String[] getButtons()
	{
		return new String[] {
				I18n.format("gui.tcmp:join"),
//				I18n.format("gui.tcmp:send_world"),
				I18n.format("gui.tcmp:unfriend")
		};
	}
	
	public boolean isEnabled(int button)
	{
		if(button != 1) return online != null;
		return true;
	}
	
	public void clickButton(int button, int mouseBtn)
	{
		if(mouseBtn != 0) return;
		
		val mc = Minecraft.getMinecraft();
		if(button == 0 && mc.currentScreen instanceof GuiFriendList)
		{
			mc.displayGuiScreen(new GuiEstablishingConnection(
					(GuiFriendList) mc.currentScreen,
					entry,
					online
			));
		}
		
		if(button == 1)
		{
			GuiScreen gs = mc.currentScreen;
			String s = I18n.format("gui.tcmp:unfriend_question");
			String s1 = "'" + profile.getName() + "' " + I18n.format("gui.tcmp:unfriend_warning");
			String s2 = I18n.format("selectServer.deleteButton");
			String s3 = I18n.format("gui.cancel");
			GuiYesNo guiyesno = new GuiYesNo((result, id) ->
			{
				if(result)
				{
					if(ClientProxy.getDatabase().getFriends().remove(entry))
					{
						TCMultiplayer.LOG.info("Unfriended {}.", profile.getName());
						ClientProxy.getDatabase().trySave();
					} else
					{
						TCMultiplayer.LOG.info("Unable to unfriend {}.", profile.getName());
					}
				}
				mc.displayGuiScreen(gs);
			}, s, s1, s2, s3, 1);
			mc.displayGuiScreen(guiyesno);
		}
	}
	
	@Override
	public void updatePosition(int slotIndex, int x, int y, float partialTicks)
	{
	
	}
	
	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
	{
		GlStateManager.color(1F, 1F, 1F, 1F);
		RenderUtil.drawGradientRect(x, y, listWidth, slotHeight, 0x55000000, 0x55000000);
		
		if(profile == null) return;
		
		Minecraft mc = Minecraft.getMinecraft();
		FontRenderer font = mc.fontRenderer;
		
		SkinHeadRenderer.renderSkin(profile, true, x + 1, y + 1, slotHeight - 4, slotHeight - 4);
		BlinkingText.renderText(x + slotHeight, y + 3, profile.getName(), 0xFFFFFFFF, true, false, 0.25F, 1F);
		
		String time = online != null ? "Online" : "Offline";
		BlinkingText.renderText(x + listWidth - font.getStringWidth(time) / 2F - 2, y + 2.5F, time, online != null ? 0xFF66FF66 : 0xFFFF6666, true, false, 0.25F, 0.5F);
		
		GlStateManager.color(1F, 1F, 1F, 1F);
		
		int xOff = slotHeight;
		int yf = y + 4 + font.FONT_HEIGHT;
		String[] buttons = getButtons();
		hoveredButton = -1;
		for(int i = 0; i < buttons.length; i++)
		{
			String label = buttons[i];
			
			int lwidth = font.getStringWidth(label) + 3;
			mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/widgets.png"));
			
			int xf = x + xOff;
			
			boolean hovered = mouseX >= xf && mouseY >= yf && mouseX <= xf + lwidth && mouseY <= yf + 11;
			if(hovered) hoveredButton = i;
			int j = isEnabled(i) ? (hovered ? 2 : 1) : 0;
			GlStateManager.pushMatrix();
			float mul = 11 / 20F;
			float width = lwidth / mul;
			GlStateManager.translate(xf, yf, 0);
			GlStateManager.scale(mul, mul, mul);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderUtil.drawTexturedModalRect(0, 0, 0, 46 + j * 20, width / 2, 20);
			RenderUtil.drawTexturedModalRect(width / 2, 0, 200 - width / 2, 46 + j * 20, width / 2, 20);
			GlStateManager.popMatrix();
			
			font.drawString(label, xf + 2, yf + 2, 0xFFFFFFFF);
			xOff += lwidth + 2;
		}
	}
	
	@Override
	public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
	{
		return false;
	}
	
	@Override
	public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
	{
	
	}
}