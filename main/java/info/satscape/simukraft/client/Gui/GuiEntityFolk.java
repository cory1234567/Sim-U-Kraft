package info.satscape.simukraft.client.Gui;

import java.util.ArrayList;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import info.satscape.simukraft.common.FolkData;
import info.satscape.simukraft.common.Relationship;

public class GuiEntityFolk extends GuiScreen
{
    private static final ResourceLocation myBackgroundTexture
        = new ResourceLocation("satscapesimukraft", "textures/gui/guiFolk.png");

    private int currentPage = 0;
    private int mouseCount = 0;
    private FolkData theFolk;
    private EntityPlayer entityplayer;
    private ArrayList<Relationship> folksRelationships;
    private int relOffset = 0;

    public GuiEntityFolk(FolkData f, EntityPlayer entityplayer)
    {
        theFolk = f;
        this.entityplayer = entityplayer;
        folksRelationships = Relationship.getRelationshipsFor(theFolk);
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

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(myBackgroundTexture);  // New in 1.6.2
        int posX = (this.width - 256) / 2;
        drawTexturedModalRect(posX, 5, 0, 0, 256, 256);

        if (currentPage == 0)
        {
            int left = this.width / 2 - 120;
            int sec = this.width / 2;
            drawCenteredString(fontRenderer, "Hello " + entityplayer.username
                               + ", Here's my information...", width / 2, 10, 0xFFFFFF);
            fontRenderer.drawString("Name", left, 27, 0x000000);
            fontRenderer.drawString(theFolk.name, sec, 27, 0x000080);
            fontRenderer.drawString("Age", left, 37, 0x000000);

            if (theFolk.age > 1)
            {
                fontRenderer.drawString(theFolk.age + " years old", sec, 37,
                                        0x000080);
            }
            else if (theFolk.age == 1)
            {
                fontRenderer.drawString("1 year old", sec, 37,
                                        0x000080);
            }
            else
            {
                fontRenderer.drawString("Less than 1 year", sec, 37,
                                        0x000080);
            }

            String words = "";

            if (theFolk.gender == 0)
            {
                words = "Male ";
            }
            else
            {
                words = "Female ";
            }

            if (theFolk.age >= 18)
            {
                words += "adult";
            }
            else
            {
                words += "child";
            }

            fontRenderer.drawString("Gender", left, 47, 0x000000);
            fontRenderer.drawString(words, sec, 47, 0x000080);

            if (theFolk.employedAt == null)
            {
                words = "unemployed";
            }
            else
            {
                words = theFolk.vocation.toString();
            }

            fontRenderer.drawString("Job", left, 57, 0x000000);

            if (theFolk.age >= 18)
            {
                fontRenderer.drawString(words, sec, 57, 0x000080);
            }
            else
            {
                fontRenderer.drawString("N/A", sec, 57, 0x000080);
            }

            if (theFolk.age >= 18)
            {
                if (theFolk.getHome() == null)
                {
                    words = "Homeless";
                }
                else
                {
                    words = "Tennant";
                }
            }
            else
            {
                words = "Living with parents";
            }

            fontRenderer.drawString("Housing status", left, 67, 0x000000);
            fontRenderer.drawString(words, sec, 67, 0x000080);

            if (!Relationship.isFolkLivingWithSomeone(theFolk))
            {
                words = "Single";
            }
            else
            {
                String who = "You";
                FolkData whofd = Relationship.isFolkLivingWithSomeone(theFolk,
                                 true);

                if (whofd != null)
                {
                    who = whofd.name;
                }

                words = "Living with " + who;
            }

            fontRenderer.drawString("Relationship", left, 77, 0x000000);
            fontRenderer.drawString(words, sec, 77, 0x000080);
            words = "Unknown";

            try
            {
                words = theFolk.action.toString();
            }
            catch (Exception e)
            {
            }

            fontRenderer.drawString("Status", left, 87, 0x000000);
            fontRenderer.drawString(words, sec, 87, 0x000080);
            fontRenderer.drawString("Building skill level", left, 97, 0x000000);
            fontRenderer.drawString((int) theFolk.levelBuilder + " of 10", sec,
                                    97, 0x000080);
            double w = 128 * (((theFolk.levelBuilder % 1) * 1000) / 1000d);
            this.drawGradientRect(sec, 97, (int) w + sec, 105, 0x50FF0000,
                                  0x50FFFF00); // Left,top,right,bottom, ARGB
            fontRenderer.drawString("Mining skill level", left, 107, 0x000000);
            fontRenderer.drawString((int) theFolk.levelMiner + " of 10", sec,
                                    107, 0x000080);
            w = 128 * (((theFolk.levelMiner % 1) * 1000) / 1000d);
            this.drawGradientRect(sec, 107, (int) w + sec, 115, 0x50FF0000,
                                  0x50FFFF00); // Left,top,right,bottom, ARGB

            if (theFolk.levelSoldier < 1.0f)
            {
                theFolk.levelSoldier = 1.0f;
            }

            fontRenderer.drawString("Soldier skill level", left, 117, 0x000000);
            fontRenderer.drawString((int) theFolk.levelSoldier + " of 10", sec,
                                    117, 0x000080);
            w = 128 * (((theFolk.levelSoldier % 1) * 1000) / 1000d);
            this.drawGradientRect(sec, 117, (int) w + sec, 125, 0x50FF0000,
                                  0x50FFFF00); // Left,top,right,bottom, ARGB

            if (theFolk.pregnancyStage > 0.0f)
            {
                fontRenderer.drawString("Medical status", left, 127, 0x000000);
                String days = (int)(theFolk.pregnancyStage * 9) + "";

                if (days.contentEquals("0"))
                {
                    days = "Pregnant";
                }
                else if (days.contentEquals("1"))
                {
                    days = "one day pregnant";
                }
                else
                {
                    days = days + " days pregnant";
                }

                fontRenderer.drawString(days, sec,
                                        127, 0x000080);
            }

            // // page 1 : show all relationships
        }
        else if (currentPage == 1)
        {
            int left = this.width / 2 - 125;
            int disoffset = 30;
            drawCenteredString(fontRenderer, theFolk.name + "'s Relationships", width / 2, 10, 0xFFFFFF);

            for (int r = relOffset; r < folksRelationships.size(); r++)
            {
                try
                {
                    Relationship rel = folksRelationships.get(r);
                    String[] sp = rel.toStringPersepctive(theFolk).split(": ");
                    fontRenderer.drawString(sp[0], left, disoffset, 0x000000);
                    fontRenderer.drawString(sp[1], this.width / 2, disoffset, 0x000080);
                    disoffset += 10;

                    if (disoffset > 200)
                    {
                        break;
                    }
                }
                catch (Exception e) {}
            }
        }

        // theGuiTextField1.drawTextBox();
        super.drawScreen(i, j, f);
    }

    private void showPage()
    {
        buttonList.clear();
        buttonList
        .add(new GuiButton(0, 2, this.height - 22, 50, 20, "Goodbye!"));

        if (currentPage == 0)
        {
            buttonList.add(new GuiButton(1, width / 2 - 50, 140, 100, 20,
                                         "Relationships"));
        }
        else if (currentPage == 1)
        {
            buttonList.add(new GuiButton(1, 2, height - 42, 50, 20,
                                         "Back"));

            if (relOffset > 0)
            {
                buttonList.add(new GuiButton(2, width / 2 - 125, 8, 20, 20, "<"));
            }

            int rels = folksRelationships.size();

            if (rels - relOffset > 18)
            {
                buttonList.add(new GuiButton(3, width / 2 + 105, 8, 20, 20, ">"));
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        if (!guibutton.enabled)
        {
            return;
        }

        if (guibutton.id == 0) // /cancel button
        {
            mc.currentScreen = null;
            mc.setIngameFocus();
        }

        if (currentPage == 0)
        {
            if (guibutton.displayString.contentEquals("Relationships"))
            {
                currentPage = 1;
                showPage();
            }
        }
        else if (currentPage == 1)     // relationship page
        {
            if (guibutton.displayString.contentEquals("Back"))
            {
                currentPage = 0;
                showPage();
            }

            if (guibutton.displayString.contentEquals("<"))
            {
                relOffset -= 18;

                if (relOffset < 0)
                {
                    relOffset = 0;
                }

                showPage();
            }

            if (guibutton.displayString.contentEquals(">"))
            {
                relOffset += 18;
                showPage();
            }
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
