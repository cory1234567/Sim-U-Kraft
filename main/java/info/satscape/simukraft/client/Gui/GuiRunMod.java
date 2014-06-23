package info.satscape.simukraft.client.Gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSmallButton;

import org.lwjgl.input.Mouse;
import info.satscape.simukraft.common.ModSimukraft;

public class GuiRunMod extends GuiScreen
{
    public boolean running = true;
    private int mouseCount = 0;

    public boolean doesGuiPauseGame()
    {
        return true;
    }

    public GuiRunMod()
    {
    }

    public void initGui()
    {
        buttonList.add(new GuiSmallButton(0, (width / 2 - 75) , 40, "Do NOT run Sim-U-Kraft"));
        buttonList.add(new GuiSmallButton(1, (width / 2 - 75) , 90, "Normal Mode"));
        buttonList.add(new GuiSmallButton(2, (width / 2 - 75) , 140, "Creative Mode"));
        buttonList.add(new GuiSmallButton(3, (width / 2 - 75) , 190, "Hardcore Mode"));
    }

    public void drawScreen(int i, int j, float f)
    {
        try
        {
            if (mouseCount < 10)
            {
                mouseCount++;
                Mouse.setGrabbed(false);
            }

            drawDefaultBackground();
            drawCenteredString(fontRenderer, "Please choose the game mode for Sim-U-Kraft", width / 2, 20, 0xffffff);
            drawCenteredString(fontRenderer, "This mode switches off Sim-U-kraft for this world", width / 2, 60, 0xffff00);
            drawCenteredString(fontRenderer, "Ideal for beginners and experts. Not too challenging.", width / 2, 110, 0xffff00);
            drawCenteredString(fontRenderer, "No money needed, everything free, no blocks required, be creative!", width / 2, 160, 0xffff00);
            drawCenteredString(fontRenderer, "Builders require ALL blocks, harder gameplay", width / 2, 210, 0xffff00);
        }
        catch (Exception e)
        {
        }

        super.drawScreen(i, j, f);
    }

    protected void actionPerformed(GuiButton guibutton)
    {
        if (guibutton.id == 0)   //do not run
        {
           // ModSimukraft.states.runMod = 0;
            ModSimukraft.states.gameModeNumber = 10;
        }
        else if (guibutton.id == 1)      // normal
        {
           // ModSimukraft.states.runMod = 1;
            ModSimukraft.states.gameModeNumber = 0;
            ModSimukraft.proxy.getClientWorld().playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, "satscapesimukraft:welcome", 1.0f, 1.0f, false);
        }
        else if (guibutton.id == 2)    //creative
        {
           // ModSimukraft.states.runMod = 1;
            ModSimukraft.states.gameModeNumber = 1;
            ModSimukraft.proxy.getClientWorld().playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, "satscapesimukraft:welcome", 1.0f, 1.0f, false);
        }
        else if (guibutton.id == 3)    //hardcore
        {
          //  ModSimukraft.states.runMod = 1;
            ModSimukraft.states.gameModeNumber = 2;
            ModSimukraft.proxy.getClientWorld().playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, "satscapesimukraft:welcome", 1.0f, 1.0f, false);
        }

        ModSimukraft.states.saveStates();
        this.running = false;
        mc.currentScreen = null;
        mc.setIngameFocus();
    }
}
