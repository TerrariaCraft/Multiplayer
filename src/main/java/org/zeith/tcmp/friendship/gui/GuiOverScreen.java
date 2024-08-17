package org.zeith.tcmp.friendship.gui;

import com.zeitheron.hammercore.utils.color.ColorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public abstract class GuiOverScreen
		extends GuiScreen
{
	public final GuiScreen parent;
	
	public GuiOverScreen(GuiScreen parent)
	{
		this.parent = parent;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
	}
	
	@Override
	public void onResize(Minecraft mcIn, int w, int h)
	{
		super.onResize(mcIn, w, h);
		parent.onResize(mcIn, w, h);
	}
	
	protected abstract void drawOverlay(int mouseX, int mouseY, float partialTicks);
	
	protected void close()
	{
		mc.displayGuiScreen(parent);
		if(this.mc.currentScreen == null) this.mc.setIngameFocus();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		parent.drawScreen(-1000, -1000, partialTicks);
		
		int color = ColorHelper.packARGB(0.5F, 0F, 0F, 0F);
		this.drawGradientRect(0, 0, this.width, this.height, color, color);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		drawOverlay(mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void updateScreen()
	{
		parent.updateScreen();
		super.updateScreen();
	}
}