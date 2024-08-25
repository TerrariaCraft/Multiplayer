package org.zeith.tcmp.friendship.gui.lst;

import com.zeitheron.hammercore.utils.color.ColorHelper;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import org.zeith.hammeranims.api.McUtil;
import org.zeith.hammerlib.util.mcf.Resources;
import org.zeith.tcmp.TCMultiplayer;
import org.zeith.tcmp.friendship.FriendEntry;
import org.zeith.tcmp.friendship.gui.McAuth;
import org.zeith.tcmp.friendship.gui.req.GuiFriendRequests;
import org.zeith.tcmp.friendship.net.FriendshipConstants;
import org.zeith.tcmp.friendship.net.OnlinePerson;
import org.zeith.tcmp.proxy.ClientProxy;
import org.zeith.terraria.api.client.gui.UVs;
import org.zeith.terraria.client.gui.api.GuiBaseMainBG;
import org.zeith.terraria.client.gui.mc.buttons.GuiButtonSpriteAndBg;
import org.zeith.terraria.client.gui.oow.GuiTerrariaMainMenu;
import org.zeith.terraria.client.player.characters.ClientActiveCharacterData;
import org.zeith.terraria.init.SoundsTC;

import java.io.IOException;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(Side.CLIENT)
public class GuiFriendList
		extends GuiBaseMainBG
{
	public final List<FriendGuiEntry> friends = new ArrayList<>();
	public FriendsList friendsList;
	
	private final GuiTerrariaMainMenu parent;
	public int ticksExisted;
	
	GuiButton refreshBtn;
	
	public GuiFriendList(GuiTerrariaMainMenu parent)
	{
		setFrom(parent);
		this.parent = parent;
		McAuth.createServices();
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		Map<PublicKey, OnlinePerson> onlines = new HashMap<>();
		for(FriendGuiEntry e : this.friends)
		{
			onlines.put(e.entry.getTheirPublicKey(), e.online);
		}
		
		this.friends.clear();
		List<FriendEntry> friends = ClientProxy.getDatabase().getFriends();
		for(int i = 0, friendsSize = friends.size(); i < friendsSize; i++)
		{
			FriendEntry friend = friends.get(i);
			val fge = new FriendGuiEntry(i, friend);
			fge.online = onlines.get(friend.getTheirPublicKey());
			this.friends.add(fge);
		}
		
		friendsList = new FriendsList(this.mc, width - 64, this.height, 21, height - 24);
		friendsList.setSlotXBoundsFromLeft(32);
		friendsList.registerScrollButtons(7, 8);
		
		addButton(new GuiButton(0, width / 2 - 50 + 110, height - 22, 100, 20, I18n.format("gui.back")));
		addButton(new GuiButton(1, width / 2 - 50 - 110, height - 22, 100, 20, I18n.format("gui.tcmp:friend_requests")));
		refreshBtn = addButton(new GuiButton(2, width / 2 - 50, height - 22, 100, 20, I18n.format("gui.tcmp:refresh")));
		
		refreshFriendList();
	}
	
	@Override
	public void updateScreen()
	{
		++ticksExisted;
		super.updateScreen();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
		
		GlStateManager.disableAlpha();
		this.draw25D(mouseX, mouseY, partialTicks);
		GlStateManager.enableAlpha();
		
		int color = ColorHelper.packARGB(Math.min((ticksExisted + partialTicks) / 20F, 1F) * 0.35F, 0F, 0F, 0F);
		int color2 = ColorHelper.packARGB(Math.min((ticksExisted + partialTicks) / 20F, 1F) * 0.5F, 0F, 0F, 0F);
		
		this.drawGradientRect(0, 0, this.width, this.height, color, color);
		
		int w = 32;
		this.drawGradientRect(w, 21, this.width - w, this.height - 24, color, color);
		this.drawGradientRect(w * 4, 2, this.width - w * 4, 16, color2, color2);
		this.drawGradientRect(w * 4, 16, this.width - w * 4, 21, color2, 0xFF000000);
		
		drawCenteredString(fontRenderer, I18n.format("gui.tcmp:friend_list"), width / 2, (21 - fontRenderer.FONT_HEIGHT) / 2, 0xFFFFFFFF);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		friendsList.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
			throws IOException
	{
		for(int i = 0; i < friends.size(); i++)
		{
			val r = friends.get(i);
			if(r.hoveredButton >= 0)
			{
				if(r.isEnabled(r.hoveredButton))
				{
					r.clickButton(r.hoveredButton, mouseButton);
					mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundsTC.MENU_TICK.sound, 1F));
				}
				return;
			}
		}
		friendsList.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public void handleMouseInput()
			throws IOException
	{
		super.handleMouseInput();
		friendsList.handleMouseInput();
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode)
	{
		if(keyCode == Keyboard.KEY_ESCAPE) close();
		if(keyCode == Keyboard.KEY_F5)
		{
			refreshFriendList();
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton button)
			throws IOException
	{
		if(button.id == 0) close();
		if(button.id == 1) this.mc.displayGuiScreen(new GuiFriendRequests(this));
		if(button.id == 2) refreshFriendList();
		super.actionPerformed(button);
	}
	
	CompletableFuture<?> refreshTask;
	
	public void refreshFriendList()
	{
		// already in progress...
		if(refreshTask != null && !refreshTask.isDone()) return;
		refreshBtn.enabled = false;
		refreshTask = CompletableFuture.runAsync(() ->
		{
			try
			{
				Map<String, OnlinePerson> list = FriendshipConstants.list();
				for(FriendGuiEntry fr : friends)
					fr.online = list.get(fr.entry.getKey());
			} finally
			{
				refreshBtn.enabled = true;
			}
		}, McUtil.backgroundExecutor());
	}
	
	public void close()
	{
		setTo(this.parent);
		this.mc.displayGuiScreen(this.parent);
		if(this.mc.currentScreen == null) this.mc.setIngameFocus();
		mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundsTC.MENU_CLOSE.sound, 1F));
	}
	
	@SubscribeEvent
	public static void initMainMenu(GuiScreenEvent.InitGuiEvent e)
	{
		if(!(e.getGui() instanceof GuiTerrariaMainMenu)) return;
		GuiTerrariaMainMenu gtmm = (GuiTerrariaMainMenu) e.getGui();
		
		if(ClientProxy.getDatabase() == null || ClientProxy.getFriendshipServer() == null)
			return;
		
		GuiButton multiplayer = null;
		for(GuiButton guiButton : e.getButtonList())
		{
			if(guiButton.id == 2)
			{
				multiplayer = guiButton;
				break;
			}
		}
		
		int x = multiplayer != null ? multiplayer.x - 24 : 0;
		int y = multiplayer != null ? multiplayer.y : 0;
		
		e.getButtonList().add(
				new GuiButtonSpriteAndBg(-326896, x, y, 20, 20,
						UVs.fullTexture(Resources.location(TCMultiplayer.MODID, "textures/gui/friends.png")),
						UVs.fullTexture(Resources.location(TCMultiplayer.MODID, "textures/gui/friends_hovered.png"))
				).withSound(SoundsTC.MENU_OPEN.sound).withCallback(() ->
						ClientActiveCharacterData.chooseCharacter(true).thenAccept(res ->
						{
							if(!res.isCancelled())
								Minecraft.getMinecraft().displayGuiScreen(new GuiFriendList(gtmm));
						})
				)
		);
	}
	
	public class FriendsList
			extends GuiListExtended
	{
		public FriendsList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn)
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
			return friends.get(index);
		}
		
		@Override
		protected int getSize()
		{
			return friends.size();
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