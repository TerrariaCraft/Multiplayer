package org.zeith.tcmp;

import com.zeitheron.hammercore.cfg.gui.HCConfigGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.util.Collections;
import java.util.Set;

public class ConfigFactory
		implements IModGuiFactory
{
	@Override
	public void initialize(Minecraft minecraft)
	{
	
	}
	
	@Override
	public boolean hasConfigGui()
	{
		return true;
	}
	
	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen)
	{
		return new HCConfigGui(parentScreen, ConfigsTCMP.cfg, TCMultiplayer.MODID);
	}
	
	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories()
	{
		return Collections.emptySet();
	}
}