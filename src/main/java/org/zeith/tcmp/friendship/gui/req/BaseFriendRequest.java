package org.zeith.tcmp.friendship.gui.req;

import com.mojang.authlib.GameProfile;
import com.zeitheron.hammercore.client.utils.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.zeith.tcmp.friendship.gui.McAuth;
import org.zeith.terraria.client.util.BlinkingText;

public abstract class BaseFriendRequest
		implements GuiListExtended.IGuiListEntry
{
	protected GameProfile profile;
	
	public int hoveredButton;
	
	public BaseFriendRequest(GameProfile profile)
	{
		this.profile = McAuth.updateProfile(profile);
	}
	
	public abstract String getRequestType();
	
	public abstract String[] getButtons();
	
	public abstract void clickButton(int button, int mouseBtn);
	
	public abstract long getMsTillExpiry();
	
	public abstract double expiryProgress();
	
	public abstract boolean isExpired();
	
	@Override
	public void updatePosition(int slotIndex, int x, int y, float partialTicks)
	{
	}
	
	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
	{
		GlStateManager.color(1F, 1F, 1F, 1F);
		RenderUtil.drawGradientRect(x, y, listWidth, slotHeight, 0x55000000, 0x55000000);
		
		float barProgress = 1F - (float) expiryProgress();
		int color = 255 << 24 | MathHelper.hsvToRGB(0.33F * barProgress, 1F, 1F);
		RenderUtil.drawGradientRect(x + 1, y + slotHeight - 2, (listWidth - 2) * barProgress, 1, color, color);
		
		Minecraft mc = Minecraft.getMinecraft();
		FontRenderer font = mc.fontRenderer;
		
		SkinHeadRenderer.renderSkin(profile, true, x + 1, y + 1, slotHeight - 4, slotHeight - 4);
		String rt = getRequestType();
		BlinkingText.renderText(x + slotHeight, y + 3, rt, 0xFFFFFFFF, true, false, 0.25F, 1F);
		
		StringBuilder sb = new StringBuilder();
		long s = getMsTillExpiry() / 1000L;
		if(s >= 60L)
		{
			StringBuilder min = new StringBuilder(Long.toString(s / 60L));
			while(min.length() < 2) min.insert(0, "0");
			sb.append(min).append(":");
		} else sb.append("00:");
		
		{
			StringBuilder min = new StringBuilder(Long.toString(s % 60L));
			while(min.length() < 2) min.insert(0, "0");
			sb.append(min);
		}
		
		String time = sb.toString();
		BlinkingText.renderText(x + listWidth - font.getStringWidth(time) / 2F - 2, y + 2.5F, time, 0xFFFFFFFF, true, false, 0.25F, 0.5F);
		BlinkingText.renderText(x + listWidth - font.getStringWidth(profile.getName()) / 2F - 2, y + 2 + font.FONT_HEIGHT / 2F, profile.getName(), 0xFFFFFFFF, true, false, 0.25F, 0.5F);
		
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
			int j = hovered ? 2 : 1;
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