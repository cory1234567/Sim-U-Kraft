package info.satscape.simukraft.client;

import info.satscape.simukraft.common.CommonProxy.V3;
import info.satscape.simukraft.common.ModSimukraft;
import info.satscape.simukraft.common.ModSimukraft.GameMode;
import java.util.EnumSet;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.src.ModLoader;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class ClientTickHandler implements ITickHandler
{
    Minecraft mc = ModLoader.getMinecraftInstance();
    GuiScreen hud = new GuiScreen();
    Long timeSinceLastSave = 0l;

    public static int beamingStage = 1;
    public static long beamingStartedAt = 0;
    public static V3 beamingTo = null;
    public static EntityPlayer beamingPlayer = null;

    public void onTickInGame()
    {

        if (beamingTo != null)
        {
            beamingPlayer();
        }
        
        //--------------------------------
        try
        {
            if (ModSimukraft.states.gameModeNumber <= 0)
            {
                return;
            }
        }
        catch (Exception e) {}

        if (mc.currentScreen != null)
        {
            if (mc.currentScreen.toString().toLowerCase().contains("ingamemenu"))
            {
                if (System.currentTimeMillis() - timeSinceLastSave > 10000)
                {
                	
                	
                   /*
                	ModSimukraft.config.save();
                    ModSimukraft.states.saveStates();
                    Building.saveAllBuildings();
                    CourierTask.saveCourierTasksAndPoints();
                    MiningBox.saveMiningBoxes();
                    FarmingBox.saveFarmingBoxes();

                    for (int f = 0; f < ModSimukraft.theFolks.size(); f++)
                    {
                        FolkData folk = ModSimukraft.theFolks.get(f);
                        folk.updateLocationFromEntity();
                        folk.saveThisFolk();
                    }
					*/
                   // ModSimukraft.log("Saved ALL game data via in Game Menu");
                    timeSinceLastSave = System.currentTimeMillis();
                }
            }
        }


    }

    public void onGui()
    {
        //Draw the HUD
        if (mc.currentScreen == null)
        {
            String worldname = "unknown";

            try
            {
                if (ModSimukraft.states.gameModeNumber == 10)
                {
                    return;
                }

                worldname = mc.getIntegratedServer().getFolderName();
                worldname = ModLoader.getMinecraftServerInstance().getFolderName();
            }
            catch (Exception e)
            {
                //e.printStackTrace();
                hud.drawString(mc.fontRenderer, "Sim-U-Kraft is not SMP", hud.width / 2, 2, 0xffffff);
                return;
            }

            try
            {
                if (ModSimukraft.proxy.ranStartup)
                {
                    int HUDoffset = 0;

                    if (mc.thePlayer.dimension == 1)
                    {
                        HUDoffset = 20;
                    }

                    HUDoffset += ModSimukraft.configHUDoffset;

                    if (ModSimukraft.gameMode == GameMode.CREATIVE)
                    {
                        hud.drawString(mc.fontRenderer, worldname + " (" + ModSimukraft.getDayOfWeek() +
                                       ") - Population: " + ModSimukraft.theFolks.size() , hud.width / 2, 2 + HUDoffset, 0xffffff);
                    }
                    else
                    {
                        hud.drawString(mc.fontRenderer, worldname + " (" + ModSimukraft.getDayOfWeek() +
                                       ") - Population: " + ModSimukraft.theFolks.size() +
                                       "   Sim-U-credits: " + ModSimukraft.displayMoney(ModSimukraft.states.credits), hud.width / 2, 2 + HUDoffset, 0xffffff);
                    }
                }
                else
                {
                    hud.drawString(mc.fontRenderer, "Loading Sim-U-Kraft...", hud.width / 2, 2, 0xffffff);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void beamingPlayer()
    {
    	
        Minecraft mc = ModLoader.getMinecraftInstance();
        Random random = new Random();
        beamingPlayer.motionX = 0;
        beamingPlayer.motionY = 0;
        beamingPlayer.motionZ = 0;
        Double d4 = ((double) random.nextFloat() - 2D) * 2D;

        for (int p = 0; p < 20; p++)
        {
            try
            {
                mc.theWorld.spawnParticle("portal",
                                          beamingPlayer.posX + (random.nextDouble()) - 0.5,
                                          beamingPlayer.posY - 1,
                                          beamingPlayer.posZ + (random.nextDouble()) - 0.5, 0,
                                          -d4, 0);
            }
            catch (Exception e)
            {
            }

            try
            {
                mc.theWorld.spawnParticle("portal",
                                          beamingTo.x + (random.nextDouble()) - 0.5,
                                          beamingTo.y - 1,
                                          beamingTo.z + (random.nextDouble()) - 0.5, 0,
                                          -d4, 0);
            }
            catch (Exception e)
            {
            }
        }

        if (beamingStage == 1)
        {
            if (System.currentTimeMillis() - beamingStartedAt > 6000)
            {
                beamingStage = 2;
                beamingPlayer.setPositionAndUpdate(beamingTo.x, beamingTo.y, beamingTo.z);
               // PacketDispatcher.sendPacketToServer(PacketHandler
                                             //       .makePacket(beamingPlayer.entityId, "shiftplayer", beamingTo.toString(), beamingTo.theDimension));
            }
        }
        else if (beamingStage == 2)
        {
            mc.theWorld.playSound(beamingTo.x, beamingTo.y, beamingTo.z, "satscapesimukraft:beamdowntwo" , 1f, 1f, false);
            beamingStage = 3;
        }
        else if (beamingStage == 3)
        {
            if (System.currentTimeMillis() - beamingStartedAt > 10000 || beamingTo == null)
            {
                beamingTo = null;
                beamingPlayer = null;
                return;
            }
        }
    }

    public ClientTickHandler()
    {
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData)
    {
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        if (type.equals(EnumSet.of(TickType.CLIENT)))
        {
            onTickInGame();
        }

        if (type.equals(EnumSet.of(TickType.WORLDLOAD)))
        {
            System.out.println("WorldLoad event tick(client side)");
        }

        if (type.equals(EnumSet.of(TickType.RENDER)))
        {
            onGui();
        }
    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.CLIENT, TickType.WORLDLOAD, TickType.RENDER);
    }

    @Override
    public String getLabel()
    {
        return "ClientTickHandler";
    }
}
