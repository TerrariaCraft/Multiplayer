package org.zeith.tcmp.friendship.gui.connect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.input.Keyboard;
import org.zeith.hammeranims.api.McUtil;
import org.zeith.tcmp.friendship.FriendEntry;
import org.zeith.tcmp.friendship.gui.lst.GuiFriendList;
import org.zeith.tcmp.friendship.gui.req.GuiFriendRequests;
import org.zeith.tcmp.friendship.net.OnlinePerson;
import org.zeith.tcmp.friendship.net.client.FriendshipPromise;
import org.zeith.tcmp.proxy.ClientProxy;
import org.zeith.terraria.client.gui.api.GuiBaseMainBG;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class GuiEstablishingConnection
		extends GuiBaseMainBG
{
	private final GuiFriendList parent;
	private GuiFriendRequests.RequestList requestList;
	
	protected int dirtFade;
	
	protected final FriendEntry friend;
	protected final OnlinePerson person;
	
	protected CompletableFuture<ServerAddress> establishedAddress;
	
	public GuiEstablishingConnection(GuiFriendList parent, FriendEntry friend, OnlinePerson person)
	{
		this.panoramaTime = parent.panoramaTime;
		this.parent = parent;
		this.friend = friend;
		this.person = person;
		openTunnel();
	}
	
	public void openTunnel()
	{
		if(establishedAddress != null && !establishedAddress.isCancelled() && !establishedAddress.isCompletedExceptionally())
			return;
		establishedAddress = FriendshipPromise.obtainAddress(
				ClientProxy.getFriendshipServer().getApi(),
				ClientProxy.getDatabase(),
				friend,
				person
		).thenApplyAsync(addrString ->
				addrString != null && !addrString.isEmpty() ? ServerAddress.fromString(addrString) : null,
				McUtil.backgroundExecutor()
		);
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
	}
	
	@Override
	public void updateScreen()
	{
		super.updateScreen();
		
		if(dirtFade < 20)
		{
			dirtFade++;
			return;
		}
		
		if(establishedAddress != null && (establishedAddress.isDone() || establishedAddress.isCancelled()))
		{
			if(establishedAddress.isCancelled() || establishedAddress.isCompletedExceptionally())
			{
				close();
				return;
			}
			
			ServerAddress sa = establishedAddress.join();
			if(sa == null)
			{
				close();
				return;
			}
			
			FMLClientHandler.instance().showGuiScreen(new GuiConnecting(parent, mc, new ServerData(
					person.getUsername(),
					sa.getIP() + ":" + sa.getPort(),
					false
			)));
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton button)
			throws IOException
	{
		super.actionPerformed(button);
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode)
	{
		if(keyCode == Keyboard.KEY_ESCAPE) close();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
		GlStateManager.disableAlpha();
		this.draw25D(mouseX, mouseY, partialTicks);
		GlStateManager.enableAlpha();
		drawBackground(0, Math.round(Math.min(dirtFade + partialTicks, 20) / 20F * 255F));
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		drawCenteredString(fontRenderer, "Joining " + person.getUsername() + "...", width / 2, height / 2, 0xFFFFFF);
	}
	
	public void drawBackground(int tint, int alpha)
	{
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		this.mc.getTextureManager().bindTexture(OPTIONS_BACKGROUND);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		float f = 32.0F;
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		bufferbuilder.pos(0.0D, this.height, 0.0D).tex(0.0D, (float) this.height / f + (float) tint).color(64, 64, 64, alpha).endVertex();
		bufferbuilder.pos(this.width, this.height, 0.0D).tex((float) this.width / f, (float) this.height / f + (float) tint).color(64, 64, 64, alpha).endVertex();
		bufferbuilder.pos(this.width, 0.0D, 0.0D).tex((float) this.width / f, tint).color(64, 64, 64, alpha).endVertex();
		bufferbuilder.pos(0.0D, 0.0D, 0.0D).tex(0.0D, tint).color(64, 64, 64, alpha).endVertex();
		tessellator.draw();
	}
	
	public void close()
	{
		this.parent.panoramaTime = this.panoramaTime;
		this.mc.displayGuiScreen(this.parent);
		if(this.mc.currentScreen == null) this.mc.setIngameFocus();
		if(establishedAddress != null) establishedAddress.cancel(true);
	}
}