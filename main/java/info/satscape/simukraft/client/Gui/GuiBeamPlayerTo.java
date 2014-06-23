package info.satscape.simukraft.client.Gui;

import info.satscape.simukraft.client.ClientTickHandler;
import info.satscape.simukraft.common.CommonProxy.V3;
import info.satscape.simukraft.common.CourierTask;
import info.satscape.simukraft.common.ModSimukraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.input.Keyboard;

public class GuiBeamPlayerTo extends GuiScreen
{
    private EntityPlayer thePlayer = null;

    public GuiBeamPlayerTo(EntityPlayer thePlayer)
    {
        this.thePlayer = thePlayer;
    }

    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void updateScreen()
    {
        //theGuiTextField1.updateCursorCounter();
    }

    @Override
    public void initGui()
    {
        initscreen();
    }

    private void initscreen()
    {
        buttonList.clear();
        buttonList.add(new GuiButton(0, 5, 5, 50, 20, "Cancel"));
        int x = 10, y = 40, idx = 2;

        for (int f = 0; f < ModSimukraft.theCourierPoints.size(); f++)
        {
            V3 cpoint = ModSimukraft.theCourierPoints.get(f);
            buttonList.add(new GuiButton(idx, x, y, 110, 20, cpoint.name));
            idx++;
            x += 110;

            if ((x + 110) > width)
            {
                x = 10;
                y += 20;
            }

            if ((y + 20) > (height - 50))
            {
                break;
            }
        }
    }

    @Override
    public void drawScreen(int i, int j, float f)
    {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, "Choose a point to beam to...", width / 2, 17, 0xffffff);

        if (ModSimukraft.theCourierPoints.size() == 0)
        {
            drawCenteredString(fontRenderer, "You don't have any courier/beaming points!", width / 2, 37, 0xffa0a0);
            drawCenteredString(fontRenderer, "Place a single Sim-U-Marker down and right click it", width / 2, 57, 0xffa0a0);
            drawCenteredString(fontRenderer, "to make one. You can then beam there using ANY control box.", width / 2, 77, 0xffa0a0);
        }

        super.drawScreen(i, j, f);
    }

    @Override
    public void actionPerformed(GuiButton guibutton)
    {
        if (!guibutton.enabled)
        {
            return;
        }

        if (guibutton.id == 0) // /cancel button
        {
            mc.displayGuiScreen(null);
            return;
        }

        String name = guibutton.displayString.trim();
        V3 v = CourierTask.getCourierPoint(name);
        V3 safePoint = v.clone();
        safePoint.y++;

        try
        {
            mc.theWorld.playSound(this.thePlayer.posX, this.thePlayer.posY, this.thePlayer.posZ, "satscapesimukraft:beamdown", 1.0f, 1.0f, false);
            mc.theWorld.playSound(safePoint.x, safePoint.y, safePoint.z, "satscapesimukraft:beamdown" , 1f, 1f, false);
        }
        catch (Exception e) {}  //don't care - sometimes NPEs

        ModSimukraft.sendChat("Beaming you to " + name);
        mc.displayGuiScreen(null);
        ClientTickHandler.beamingPlayer = thePlayer;
        ClientTickHandler.beamingStage = 1;
        ClientTickHandler.beamingStartedAt = System.currentTimeMillis();
        ClientTickHandler.beamingTo = safePoint.clone();
    }

    public GuiButton getButtonWithId(int id)
    {
        GuiButton retbut;

        for (int x = 0; x < buttonList.size(); x++)
        {
            retbut = (GuiButton) buttonList.get(x);

            if (retbut.id == id)
            {
                return retbut;
            }
        }

        return null;
    }

    @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
        mc.setIngameFocus();
    }

    @Override
    public void keyTyped(char c, int i)
    {
        if (i == 1) //escape
        {
            mc.displayGuiScreen(null);
            return;
        }

        //((GuiButton)buttonList.get(0)).enabled = theGuiTextField1.getText().trim().length() > 0;
    }
}
