package info.satscape.simukraft.client.Gui;


import info.satscape.simukraft.common.TileEntityWindmill;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiWindmill extends GuiScreen {
	private static final ResourceLocation myBackgroundTexture
    = new ResourceLocation("satscapesimukraft", "textures/gui/guiFolk.png");

 private int mouseCount = 0;
 private EntityPlayer entityplayer;
 private TileEntityWindmill theWindmillTileEntity;


 public GuiWindmill(EntityPlayer thePlayer, TileEntityWindmill TEwindmill)
 {
     this.entityplayer=thePlayer;
     this.theWindmillTileEntity = TEwindmill;
 }
 


 public boolean doesGuiPauseGame()
 {
     return false;
 }

 @Override
 public void updateScreen()
 {
     // theGuiTextField1.updateCursorCounter();
 }

 @Override
 public void initGui()
 {
     Keyboard.enableRepeatEvents(true);
     showPage();
 }

 @Override
 public void drawScreen(int i, int j, float f)
 {
     if (mouseCount < 10)
     {
         mouseCount++;
         Mouse.setGrabbed(false);
     }

     try {
     GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
     this.mc.renderEngine.bindTexture(myBackgroundTexture);  // New in 1.6.2
     int posX = (this.width - 256) / 2;
     drawTexturedModalRect(posX, 5, 0, 0, 256, 256);

     drawCenteredString(fontRenderer, "Sim-U-Windmill", width / 2, 10, 0xFFFFFF);
     int left = this.width / 2 - 120;
     
     fontRenderer.drawString("Place at least one chest next to the Windmill,", left, 27, 0x000000);
     fontRenderer.drawString("then place Iron ore, Gold ore or Cobblestone", left, 37, 0x000000);
     fontRenderer.drawString("into the chest. Each ore block will be processed", left, 47, 0x000000);
     fontRenderer.drawString("into 2 x ore granules. These granules will be", left, 57, 0x000000);
     fontRenderer.drawString("placed back into the chest. You can then put", left, 67, 0x000000);
     fontRenderer.drawString("these into any furnace to produce ingots.", left, 77, 0x000000);
     
     drawCenteredString(fontRenderer, "Contents", width / 2, 100, 0xFFFFFF);
     ItemStack is=theWindmillTileEntity.getStackInSlot(0);
     String status="";
     
     if (is==null) {
    	 this.drawCenteredString(fontRenderer,"Empty", width/2,120, 0xFFFF00);
    	 status="Idle, no ore to process";
     } else {
    	 this.drawCenteredString(fontRenderer,is.stackSize+" x "+ is.getDisplayName(), width/2,120, 0xFFFF00);
    	 status="Granulating "+is.getDisplayName()+" " + (int)theWindmillTileEntity.processTime/20+" %";
     }
     int w = (int) (2.4f* (((theWindmillTileEntity.processTime) /20)));
     this.drawGradientRect(left, 140, (int) w + left, 150, 0x50FF0000,0x50FFFF00); // Left,top,right,bottom, ARGB
     drawCenteredString(fontRenderer, status , width / 2, 141, 0xFFFFFF);
     
     drawCenteredString(fontRenderer, theWindmillTileEntity.errorString, width/2,190,0xFF0000);
     }catch(Exception e) {}
     
     
     super.drawScreen(i, j, f);
 }

 private void showPage()
 {
     buttonList.clear();


 }

 @Override
 protected void actionPerformed(GuiButton guibutton)
 {
     if (!guibutton.enabled)
     {
         return;
     }

 
 }

 @Override
 public void onGuiClosed()
 {
     Keyboard.enableRepeatEvents(false);
 }

 @Override
 protected void keyTyped(char c, int i)
 {
     if (i == 1)   // escape and dont save
     {
         mc.currentScreen = null;
         mc.setIngameFocus();
         return;
     }
 }

 @Override
 protected void mouseClicked(int i, int j, int k)
 {
     // theGuiTextField1.mouseClicked(i, j, k);
     super.mouseClicked(i, j, k);
 }





}
