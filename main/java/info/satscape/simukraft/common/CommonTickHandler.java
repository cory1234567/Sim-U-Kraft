package info.satscape.simukraft.common;

import info.satscape.simukraft.client.Gui.GuiRunMod;
import info.satscape.simukraft.common.CommonProxy.V3;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.server.FMLServerHandler;

public class CommonTickHandler implements ITickHandler
{
    private World serverWorld = null;
    Long lastSecondTickAt = 0l;
    Long lastMinuteTickAt = 0l;

    GuiRunMod runModui = null;
    String currentWorld = "";
    Minecraft mc = ModLoader.getMinecraftInstance();
    long lastReset = 0;

    public void onTickInGame()
    {
        if (mc.currentScreen != null)
        {
            if (mc.currentScreen.toString().toLowerCase().contains("guimainmenu"))
            {
            	ModSimukraft.log.info("CommTH: in Gui Main menu");
            }
        }

        if (ModSimukraft.states.gameModeNumber == 10)
        {
            ModSimukraft.proxy.ranStartup = true;
            return;
        }

        Long now = System.currentTimeMillis();

        if (serverWorld != null)
        {
            //fire onUpdate() for each folkData
            FolkData.triggerAllUpdates();
            //handle day-night-day transitions
            ModSimukraft.dayTransitionHandler();

            //if a farm needs upgarding, incrementally upgrade it
            if (ModSimukraft.farmToUpgrade != null)
            {
                ModSimukraft.upgradeFarm();
            }

            if (ModSimukraft.demolishBlocks.size() > 0) //and demolishing buildings
            {
                ModSimukraft.demolishBlocks();
            }
        }

        // ***** ONCE A SECOND
        if (now - lastSecondTickAt > 1000)
        {
            if (serverWorld == null)
            {
                try
                {
                    //this is a crazy way to delay running stuff until things have spawned! :-)
					//Also, this won't work in MC1.7+ as there's no ModLoader class in Forge any more...good luck with that!
                    for (World w : ModLoader.getMinecraftServerInstance().worldServers)
                    {
                        if (w.loadedEntityList.size() > 0)
                        {
                            serverWorld = w;
                            currentWorld = ModSimukraft.getSavesDataFolder();
                            ModSimukraft.log.info("CommTH: Startup - set serverWorld/currentWorld");
                            ModSimukraft.resetAndLoadNewWorld();
                            PacketDispatcher.sendPacketToAllPlayers(PacketHandler.makePacket("", "gamereset", ""));
                            break;
                        }
                    }
                }
                catch (Exception e)
                {
                    serverWorld = null;   //will exception until first player has spawned!
                }
            }
            else      //used to detect world change - Still a bug with this, not unloading world when player switches via main menu
            {
                if (!currentWorld.contentEquals(ModSimukraft.getSavesDataFolder()))
                {
                    if (now - lastReset >30000) {
                    	ModSimukraft.log.info("currentWorld="+currentWorld+"     getSaves="+ModSimukraft.getSavesDataFolder());
	                	currentWorld = ModSimukraft.getSavesDataFolder();
	                    ModSimukraft.proxy.ranStartup = false;
	                    ModSimukraft.resetAndLoadNewWorld();
	                    PacketDispatcher.sendPacketToAllPlayers(PacketHandler.makePacket("", "gamereset", ""));
                    }
                }

                //STOP THE RAIN MOD - Implemented this when I had a world where it rained ALL THE F***ING TIME!
                if (serverWorld.isRaining() && serverWorld.getWorldInfo().getRainTime() > 1 &&  ModSimukraft.configStopRain == true)
                {
                    serverWorld.getWorldInfo().setRainTime(2);  //setting to 1 or 0 doesn't work every time.
                }
            }


            // ONCE A SECOND EVERY SECOND
            lastSecondTickAt = now;
            
            
        }

        
        // ***** ONCE A MINUTE
        if (serverWorld != null && System.currentTimeMillis() - lastMinuteTickAt > 60000)
        {
            if (lastMinuteTickAt > 0)
            {
                Long start = System.currentTimeMillis();
                FolkData.generateNewFolk(serverWorld);
                ModSimukraft.states.saveStates();
                Building.checkTennants();
                Building.saveAllBuildings();
                CourierTask.saveCourierTasksAndPoints();
                MiningBox.saveMiningBoxes();
                FarmingBox.saveFarmingBoxes();
                Relationship.saveRelationships();
                //PathBox.savePathBoxes();
                ModSimukraft.log.info("CTH: Saved game data in " + (System.currentTimeMillis() - start) + " ms");
            }

            lastMinuteTickAt = now;
        }
    }

    public void resetSimUKraft()
    {
        //this resets everything first, if the player has switched worlds, gets hit several times due to weird MC GUI switching,
        // so lastReset stops it from running more than once every 30 seconds.
        if (System.currentTimeMillis() - lastReset > 30000)
        {
            
            lastReset = System.currentTimeMillis();
            Side side = cpw.mods.fml.common.FMLCommonHandler.instance().getEffectiveSide();
            
            ModSimukraft.log.info(side.toString()+"-side CommTH: resetSimUKraft()");
        }
    }

    /** runs when a world has loaded, so we can set everything up */
    private void startingWorld()
    {
        if (!ModSimukraft.proxy.ranStartup)
        {
           // TODO: no longer used
           

        }
    }

    ////////////////////////////////////////////////
    public CommonTickHandler()
    {
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData)
    {
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        if (type.equals(EnumSet.of(TickType.SERVER)))
        {
            onTickInGame();
        }

        if (type.equals(EnumSet.of(TickType.WORLDLOAD)))
        {
            System.out.println("WorldLoad event tick");
        }
    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.SERVER, TickType.WORLDLOAD); //add more types?
    }

    @Override
    public String getLabel()
    {
        return "CommonTickHandler";
    }
}
