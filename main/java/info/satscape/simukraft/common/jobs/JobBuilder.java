package info.satscape.simukraft.common.jobs;

import info.satscape.simukraft.common.Building;
import info.satscape.simukraft.common.EntityConBox;
import info.satscape.simukraft.common.FolkData;
import info.satscape.simukraft.common.ModSimukraft;
import info.satscape.simukraft.common.CommonProxy.V3;
import info.satscape.simukraft.common.FolkData.FolkAction;
import info.satscape.simukraft.common.ModSimukraft.GameMode;

import java.io.Serializable;
import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.src.FMLRenderAccessLibrary;
import net.minecraft.world.World;

public class JobBuilder extends Job implements Serializable
{
    private static final long serialVersionUID = -1177665807904279141L;

    public Stage theStage;
    public FolkData theFolk = null;
    public Vocation vocation = null;

    public int runDelay = 1000;
    public long timeSinceLastRun = 0;

    private transient ArrayList<IInventory> constructorChests = new ArrayList<IInventory>();
    private transient Building theBuilding = null;
    private transient EntityConBox theConBox = null;
    private transient long lastNotifiedOfMaterials = 0;

    /**
     * used to delay the sound effect so it only fires every 2 seconds
     * regardless of build delay
     */
    private transient long soundLastPlayed = 0l;

    int l = 0, ftb = 0, ltr = 0; // 3d build loops
    int xo = 0, zo = 0, acount = 0;
    int cx, cy, cz, ex, ey, ez, bx = 0, by = 0, bz = 0;

    public JobBuilder()
    {
        // not used
    }

    public JobBuilder(FolkData folk)
    {
        theFolk = folk;

        if (theStage == null)
        {
            theStage = Stage.IDLE;
        }

        if (theFolk == null)
        {
            return;
        } // is null when first employing, this is for next day(s)

        if (theFolk.destination == null)
        {
            theFolk.gotoXYZ(theFolk.employedAt, null);
        }

        this.theBuilding = theFolk.theBuilding;
    }

    public void resetJob()
    {
        theStage = Stage.IDLE;
    }

    @Override
    public void onUpdate()
    {
        if (theFolk == null)
        {
            return;
        }

        super.onUpdate();

        //theFolk.levelBuilder=10;
        //ModSimukraft.states.credits=100000;

        if (!ModSimukraft.isDayTime())
        {
            theStage = Stage.IDLE;
        }

        super.onUpdateGoingToWork(theFolk);

        if (theStage == Stage.WAITINGFORRESOURCES)
        {
            runDelay = 3000;

            if (theBuilding != null)
            {
            }
        }

        if (theStage == Stage.INPROGRESS)
        {
            if (step == 1)
            {
                runDelay = (int)(2000 / theFolk.levelBuilder);
            }
        }

        if (System.currentTimeMillis() - timeSinceLastRun < runDelay)
        {
            return;
        }

        timeSinceLastRun = System.currentTimeMillis();

        if (theFolk.theirJob != null)
        {
            if (theFolk.vocation != Vocation.BUILDER)
            {
                theFolk.selfFire();
                return;
            }
        }

        theFolk.updateLocationFromEntity();
        int dist = theFolk.location.getDistanceTo(theFolk.employedAt);

        if (dist <= 3 && theStage == Stage.WORKERASSIGNED)
        {
            theFolk.action = FolkAction.ATWORK;
            theFolk.statusText = "Arrived at work";
            theStage = Stage.BLUEPRINT;
        }

        if (dist < 10 && theStage == Stage.WORKERASSIGNED && theFolk.destination == null)
        {
            theFolk.action = FolkAction.ATWORK;
            theFolk.statusText = "Arrived at work";
            theStage = Stage.BLUEPRINT;
        }

        // ////////////////IDLE
        if ((theStage == Stage.IDLE || theStage == Stage.WORKERASSIGNED)
                && ModSimukraft.isDayTime())
        {
            if (theFolk.action != FolkAction.ONWAYTOWORK)
            {
                theStage = Stage.WORKERASSIGNED;
            }
        }
        else if (theStage == Stage.WORKERASSIGNED)
        {
        }
        else if (theStage == Stage.BLUEPRINT)
        {
            stageBlueprint();
        }
        else if (theStage == Stage.WAITINGFORRESOURCES)
        {
            stageWaitingForResources();
        }
        else if (theStage == Stage.INPROGRESS)
        {
            stageInProgress();
        }
        else if (theStage == Stage.COMPLETE)
        {
            stageComplete();
        }
    }

    private void stageBlueprint()
    {
        theBuilding = theFolk.theBuilding;

        if (theBuilding == null)
        {
            theFolk.statusText = "Please choose which building I should build";
        }
        else
        {
            theFolk.statusText = "Looking through blueprints...";
            /*
            if (theBuilding.structure[theBuilding.structure.length - 1] == null) {
            	theBuilding.loadStructure(true);
            }
             */
            theFolk.updateLocationFromEntity();
            double dist = theFolk.location.getDistanceTo(theFolk.employedAt);

            if (dist < 4)
            {
                theFolk.stayPut = true;
            }

            if (ModSimukraft.configFolkTalking)
            {
                if (theFolk.gender == 0)
                {
                    jobWorld.playSound(
                        theFolk.location.x, theFolk.location.y,
                        theFolk.location.z, "satscapesimukraft:readym", 1f, 1f, false);
                }
                else
                {
                    jobWorld.playSound(
                        theFolk.location.x, theFolk.location.y,
                        theFolk.location.z, "satscapesimukraft:readyf", 1f, 1f, false);
                }
            }

            theStage = Stage.WAITINGFORRESOURCES;
            step = 1;

            // create the conBox entity
            if (this.theConBox == null)
            {

                World world = ModLoader.getMinecraftServerInstance()
                              .worldServerForDimension(theFolk.location.theDimension);
                this.theConBox = new EntityConBox(world);
                this.theConBox.theFolk = theFolk;
                //this.theConBox.theFolk.theBuilding.loadStructure(true);
                this.theConBox.setLocationAndAngles(theFolk.employedAt.x + 2, theFolk.employedAt.y, theFolk.employedAt.z, 0f, 0f);

                if (!world.isRemote)
                {
                    world.spawnEntityInWorld(this.theConBox);
                }
            }
        }
    }

    private void stageWaitingForResources()
    {
        theFolk.isWorking = false;

        if (step == 1)
        {
            theFolk.statusText = "Checking building resources...";
            constructorChests = inventoriesFindClosest(theFolk.employedAt, 5);

            if (constructorChests.size() == 0)
            {
                theFolk.statusText = "Please place at least one chest/storage block near to constructor block.";
            }
            else
            {
                try
                {
                    constructorChests.get(0).openChest();
                }
                catch (Exception e)
                {
                    ModSimukraft.log.info("JobBuilder:JobBuilder's chest was null");
                }

                step = 2;
            }

            int dist = theFolk.location.getDistanceTo(theFolk.employedAt);

            if (dist < 5)
            {
                theFolk.stayPut = true;
            }
        }
        else if (step == 2)
        {
            constructorChests.get(0).closeChest();
            theStage = Stage.INPROGRESS;
            step = 1;
        }
        else if (step == 3)     // this step triggers mid-build - just send them
        {
            // back in to keep checking
            if (theFolk.vocation == Vocation.BUILDER)
            {
                step = 2;
                theStage = Stage.INPROGRESS;

                if (theFolk.isSpawned())
                {
                    theFolk.updateLocationFromEntity();
                }

                int dist = theFolk.location.getDistanceTo(theFolk.employedAt);

                if (dist < 5)
                {
                    theFolk.stayPut = true;
                }
                else
                {
                    theFolk.gotoXYZ(theFolk.employedAt, null);
                }
            }
            else
            {
                theFolk.selfFire();
                return;
            }
        }
    }

    private void stageInProgress()
    {
        int blockId = 0;
        boolean alreadyPlaced = false;
        theFolk.updateLocationFromEntity();
        int dist = theFolk.location.getDistanceTo(theFolk.employedAt);

        if (dist > 5 && theFolk.destination == null)
        {
            theFolk.gotoXYZ(theFolk.employedAt, null);
            return;
        }

        if (step == 1)
        {
            cx = theFolk.employedAt.x.intValue();
            cy = theFolk.employedAt.y.intValue();
            cz = theFolk.employedAt.z.intValue();
            ex = theFolk.employedAt.x.intValue();
            ey = theFolk.employedAt.y.intValue();
            ez = theFolk.employedAt.z.intValue();
            bx = ex;
            by = ey;
            bz = ez;

            if (theBuilding.buildDirection.contentEquals("-x"))
            {
                bx = cx + 1;
            }
            else if (theBuilding.buildDirection.contentEquals("+x"))
            {
                bx = cx - 1;
            }
            else if (theBuilding.buildDirection.contentEquals("-z"))
            {
                bz = cz + 1;
            }
            else if (theBuilding.buildDirection.contentEquals("+z"))
            {
                bz = cz - 1;
            }
            else
            {
                ModSimukraft.sendChat("Can't determine the direction to build in, please stand on one of the four sides of the constructor when you right-click it");
                theFolk.selfFire();
                return;
            }

            ModSimukraft.sendChat(theFolk.name + " has started building a "
                                  + theBuilding.displayNameWithoutPK);
            theFolk.statusText = "Building " + theBuilding.displayNameWithoutPK;

            if (theBuilding == null || theBuilding.layerCount == 0)
            {
                ModSimukraft.sendChat(theFolk.name
                                      + " has misplaced the blueprints, fire them and try someone else.");
                return;
            }

            theFolk.stayPut = true;

            if (theBuilding == null)
            {
                theFolk.selfFire();
                return;
            }

            l = 0;
            ftb = 0;
            ltr = 0;
            acount = 0;
            step = 2;
            theBuilding.blockLocations.clear();
            
        }
        else if (step == 2)     // ///////////////// STEP 2
        {
            do
            {
                theFolk.statusText = "Building "
                                     + theBuilding.displayNameWithoutPK;

                if (theBuilding.buildDirection.contentEquals("+z"))
                {
                    xo = ltr;
                    zo = -ftb;
                }
                else if (theBuilding.buildDirection.contentEquals("-z"))
                {
                    xo = -ltr;
                    zo = ftb;
                }
                else if (theBuilding.buildDirection.contentEquals("+x"))
                {
                    xo = -ftb;
                    zo = -ltr;
                }
                else if (theBuilding.buildDirection.contentEquals("-x"))
                {
                    xo = ftb;
                    zo = ltr;
                }

                if (theBuilding == null)
                {
                    theFolk.selfFire();
                    return;
                }

                String[] bl = null;

                try
                {
                    bl = theBuilding.structure[acount].split(":");
                }
                catch (Exception e)
                {
                    ModSimukraft.log.warning("JobBuilder: NULL block in building, using Air instead");
                    bl = "0:0".split(":");
                }

                blockId = Integer.parseInt(bl[0]);
                int subtype = Integer.parseInt(bl[1]);

                if (blockId == Block.grass.blockID)
                {
                    blockId = Block.dirt.blockID;
                }

                if (theBuilding.type.contentEquals("other") && acount == 0)
                {
                    blockId = ModSimukraft.controlBlockId;
                    subtype = 2; //control box other
                }

                if (blockId == ModSimukraft.controlBlockId)
                {
                    try
                    {
                        theBuilding.primaryXYZ = new V3((double)(bx + xo), (double)(by + l),
                                                        (double)(bz + zo), theFolk.employedAt.theDimension);
                        theBuilding.saveThisBuilding();
                    }
                    catch (Exception e)
                    {
                        ModSimukraft.log.info("JobBuilder:build is null");
                    }
                }

                if (blockId == 999 && subtype == 999)
                {
                    theBuilding.livingXYZ = new V3((double)(bx + xo), (double)(by + l),
                                                   (double)(bz + zo), theFolk.employedAt.theDimension);
                    blockId = 0;
                    subtype = 0;
                } else if (blockId==999 && subtype>=0 && subtype <=9) {
                	V3 v3=new V3((double)(bx + xo), (double)(by + l),(double)(bz + zo), theFolk.employedAt.theDimension);
                	v3.meta=subtype;
                	theBuilding.blockSpecial.add(v3);
                	blockId = 0;
                    subtype = 0;
                }

                int currBlockId = 0;
                int currBlockMeta = 0;

                try
                {
                    currBlockId = jobWorld.getBlockId(bx + xo, by + l, bz + zo);
                    currBlockMeta = jobWorld.getBlockMetadata(bx + xo, by + l, bz + zo);
                    
                    if (blockId == currBlockId || (blockId==Block.dirt.blockID && currBlockId==Block.grass.blockID)
                    	|| (blockId==Block.grass.blockID && currBlockId==Block.dirt.blockID))
                    {
                        alreadyPlaced = true;
                    }
                    else
                    {
                        alreadyPlaced = false;
                    }
                }
                catch (Exception e)
                {
                    theFolk.selfFire();
                    return;
                }

                String want = "???";
                ItemStack wantIS = new ItemStack(blockId, 1, 0);

                if (wantIS != null && wantIS.itemID > 0)
                {
                    try
                    {
                        want = wantIS.getDisplayName();
                        if (blockId > 0)
                        {
                            theBuilding.blockLocations.add(new V3(bx + xo, by + l, bz + zo,theFolk.location.theDimension));
                        }
                        
                    }
                    catch (Exception e)
                    {
                        want = "?";
                        ModSimukraft.log.info("JobBuilder:wantItemStack nulled out, wantIS was null, blockID=" + blockId);
                    }
                }
                else
                {
                    want = "???";
                }

                if (!alreadyPlaced)   // air block it first to clear dirt
                {
                    // away
                    if (currBlockId > 0)
                    {
                        V3 blockToRemove = new V3(bx + xo, by + l, bz + zo);
                        constructorChests = inventoriesFindClosest(theFolk.employedAt, 5);
                        mineBlockIntoChests(constructorChests, blockToRemove);
                        jobWorld.setBlock(bx + xo, by + l, bz + zo, 0, 0, 0x03);
                        theFolk.isWorking = true;
                    }
                }

                if (!alreadyPlaced)
                {
                    boolean gotBlock = false;
                    boolean requiredBlocks = blockId == Block.planks.blockID
                                             || blockId == Block.cobblestone.blockID
                                             || blockId == Block.glass.blockID
                                             || blockId == Block.cloth.blockID
                                             || blockId == Block.brick.blockID
                                             || blockId == Block.dirt.blockID
                                             || blockId == Block.stoneBrick.blockID
                                             || blockId == Block.fence.blockID
                                             || blockId == Block.stone.blockID
                                             || blockId == Block.wood.blockID;

                    if (ModSimukraft.gameMode == GameMode.NORMAL)
                    {
                        if (requiredBlocks)
                        {
                            constructorChests = inventoriesFindClosest(theFolk.employedAt, 5);
                            ItemStack got = inventoriesGet(constructorChests, new ItemStack(blockId, 1, 0), false,false,-1);

                            if (got != null)
                            {
                                gotBlock = true;
                            }
                            else
                            {
                                gotBlock = false;
                            }
                        }
                        else
                        {
                            gotBlock = true;
                        }
                    }
                    else if (ModSimukraft.gameMode == GameMode.CREATIVE)
                    {
                        gotBlock = true;
                    }
                    else if (ModSimukraft.gameMode == GameMode.HARDCORE)
                    {
                        if (blockId > 0)
                        {
                            // provided blocks in hardcore mode     68=sign
                            if (blockId == Block.grass.blockID || blockId == Block.waterMoving.blockID ||
                                    blockId == Block.waterStill.blockID || blockId == Block.lavaMoving.blockID
                                    || blockId == Block.lavaStill.blockID || blockId == 68
                                    || blockId == Block.cake.blockID || blockId == Block.stoneSingleSlab.blockID
                                    || blockId == Block.woodSingleSlab.blockID || blockId == Block.woodDoubleSlab.blockID
                                    || blockId == Block.stoneDoubleSlab.blockID || blockId == Block.tilledField.blockID
                                    || blockId == Block.doorWood.blockID || blockId ==Block.doorIron.blockID
                                    || blockId == Block.bed.blockID)
                            	//// TODO: WHEN I RE-WRITE - problem here is it needs to translate blocks to items
                            {
                                gotBlock = true;
                            }
                            else
                            {
                                constructorChests = inventoriesFindClosest(theFolk.employedAt, 5);
                                ItemStack got = inventoriesGet(constructorChests, new ItemStack(blockId, 1, 0), false,false,-1);

                                if (got != null)
                                {
                                    gotBlock = true;
                                }
                                else
                                {
                                    gotBlock = false;
                                }

                                if (blockId == ModSimukraft.controlBlockId)
                                {
                                    gotBlock = true;
                                }
                            }
                        }
                        else
                        {
                            gotBlock = true;
                        }
                    }

                    if (!gotBlock)
                    {
                        theStage = Stage.WAITINGFORRESOURCES;

                        if (want.toLowerCase().contentEquals("oak wood planks"))
                        {
                            want = "Planks";
                        }

                        if (want.toLowerCase().contentEquals("oak wood"))
                        {
                            want = "Logs";
                        }

                        theFolk.statusText = "Waiting for " + want;

                        if (System.currentTimeMillis() - lastNotifiedOfMaterials > (ModSimukraft.configMaterialReminderInterval * 60 * 1000))
                        {
                            lastNotifiedOfMaterials = System.currentTimeMillis();
                            ModSimukraft.sendChat(theFolk.name + " (who's building a " + theFolk.theBuilding.displayNameWithoutPK
                                                  + ") needs more " + want);
                        }

                        step = 3;
                        return;
                    }

                    try
                    {
                    	
                        if (!alreadyPlaced)
                        {
                            try
                            {
                                if (blockId == 212 || blockId == 3211)
                                {
                                    blockId = ModSimukraft.controlBlockId;
                                }

                                // bank control boxes
                                if (blockId == ModSimukraft.controlBlockId
                                        && theBuilding.displayNameWithoutPK.toLowerCase().contentEquals("sim-u-bank"))
                                {
                                    subtype = 1;
                                }

                                //########### PLACE THE BLOCK
                                theFolk.stayPut = true;
                                jobWorld.setBlock(bx + xo, by + l,
                                                  bz + zo, blockId, subtype, 0x03);
                                jobWorld.markBlockForUpdate(bx + xo, by + l, bz + zo);

                                

                                int b4 = (int)Math.floor(theFolk.levelBuilder);

                                //theFolk.levelBuilder=10.0f;
                                if (theFolk.levelBuilder < 10.0f)
                                {
                                    theFolk.levelBuilder += (0.001 / b4);
                                }

                                int aft = (int)Math.floor(theFolk.levelBuilder);

                                if (b4 != aft)
                                {
                                    ModSimukraft.sendChat(theFolk.name + " has just levelled up to Builder Level " + aft);
                                }

                                // PLAY SOUND EFFECT every 2 seconds
                                if (System.currentTimeMillis()
                                        - soundLastPlayed >= 2000)
                                {
                                    mc.theWorld.playSound(bx + xo, by + l,
                                                          bz + zo,
                                                          "satscapesimukraft:construction", 1f, 1f, false);
                                    soundLastPlayed = System
                                                      .currentTimeMillis();
                                }

                                // spawn particles on client side
                                if (mc.theWorld.isRemote)
                                {
                                    mc.theWorld.spawnParticle("explode", bx
                                                              + xo, by + l, bz + zo, 0, 0.3f, 0);
                                    mc.theWorld.spawnParticle("explode", bx
                                                              + xo, by + l, bz + zo, 0, 0.2f, 0);
                                    mc.theWorld.spawnParticle("explode", bx
                                                              + xo, by + l, bz + zo, 0, 0.1f, 0);
                                }

                                if (blockId > 0 && ModSimukraft.gameMode != GameMode.CREATIVE)
                                {
                                    ModSimukraft.states.credits -= (0.02f);
                                }
                            }
                            catch (Exception e)
                            {
                                ModSimukraft.log.warning("JobBuilder: Possible non-existant block (from other mod) ID="
                                     + blockId);

                                try
                                {
                                    jobWorld.setBlock(bx + xo, by + l, bz + zo, 0, 0, 0x03);
                                }
                                catch (Exception e2)
                                {
                                    e2.printStackTrace();
                                }
                            } // this exceptions when another mod's block is placed down
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                //remove from requirements
                /*
                if (!want.contentEquals("???")) {
                	try {
                		Iterator it = theFolk.theBuilding.requirements.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pairs = (Map.Entry)it.next();
                            ItemStack is=(ItemStack) pairs.getKey();
                            if (is.itemID >0) {
                            	if (is.itemID==wantIS.itemID) {
                            		int left=theFolk.theBuilding.requirements.get(wantIS);
                					left--;
                					if (left>0) {
                						theFolk.theBuilding.requirements.put(wantIS, left);
                					} else {
                						theFolk.theBuilding.requirements.remove(wantIS);
                					}
                            	}
                            }
                        }
                	} catch(Exception e) {
                		ModSimukraft.log.info("JobBuilder:Builder requirement was null - no biggie"); }
                }
                */
                acount++;
                ltr++;

                if (ltr == theBuilding.ltrCount)
                {
                    ltr = 0;
                    ftb++;

                    if (ftb == theBuilding.ftbCount)
                    {
                        ftb = 0;
                        l++;

                        if (l == theBuilding.layerCount)
                        {
                            theStage = Stage.COMPLETE;
                            stageComplete();
                            return;
                        }
                    }
                }

                if (blockId == 0 || alreadyPlaced)
                {
                    runDelay = 0;
                }
                else
                {
                    if (ModSimukraft.gameMode == GameMode.CREATIVE)
                    {
                        runDelay = 0;
                    }
                    else
                    {
                        runDelay = (int)(2000 / theFolk.levelBuilder);
                    }
                }

                if (theFolk.theEntity != null)
                {
                    theFolk.theEntity.swingItem();
                }
            }
            while (blockId == 0 || alreadyPlaced);
        } // end step 2
    }

    private void stageComplete()
    {
        theFolk.isWorking = false;

        if (theBuilding != null)
        {
            if (theBuilding.buildingComplete)
            {
         //       return;
            }

            if (theBuilding != null)
            {
                theBuilding.buildingComplete = true;
                ModSimukraft.sendChat(theFolk.name
                                      + " has completed building a "
                                      + theBuilding.displayNameWithoutPK);
                ModSimukraft.proxy.getClientWorld().playSound(
                    mc.thePlayer.posX, mc.thePlayer.posY,
                    mc.thePlayer.posZ, "satscapesimukraft:cash", 1f, 1f, false);
                theBuilding.saveThisBuilding();
                theFolk.theBuilding=null;
            }
            else
            {
                ModSimukraft
                .sendChat("Error: could not set the building that "
                          + theFolk.name
                          + " was building "
                          + "to 'complete', try rebuilding right away (no cost) to try again");
            }
        }

        if (theFolk.theEntity != null)
        {
            theFolk.theEntity.setSneaking(false);
        }

        theFolk.stayPut = false;
        theFolk.selfFire();
        theStage = Stage.IDLE;
        //bodgy fix to make sure buildings are complete - probably no longer needed, original bug caused by V3 class bug
        boolean activeBuilders = false;

        for (int f = 0; f < ModSimukraft.theFolks.size(); f++)
        {
            FolkData fd = ModSimukraft.theFolks.get(f);

            if (fd.vocation == Vocation.BUILDER)
            {
                activeBuilders = true;
            }
        }

        if (!activeBuilders)
        {
            for (int b = 0; b < ModSimukraft.theBuildings.size(); b++)
            {
                Building building = ModSimukraft.theBuildings.get(b);
                building.buildingComplete = true;
            }
        }
    }

    @Override
    public void onArrivedAtWork()
    {
        int dist = 0;
        dist = theFolk.location.getDistanceTo(theFolk.employedAt);

        if (dist <= 1)
        {
            theFolk.action = FolkAction.ATWORK;
            theFolk.stayPut = true;
            theFolk.statusText = "Arrived at the building site";
            theStage = Stage.BLUEPRINT;
        }
        else
        {
            theFolk.gotoXYZ(theFolk.employedAt, null);
        }
    }

    public enum Stage
    {
        IDLE, WORKERASSIGNED, BLUEPRINT, WAITINGFORRESOURCES, INPROGRESS, COMPLETE;

        @Override
        public String toString()
        {
            String ret = "";

            if (this == IDLE)
            {
                ret = "Idle";
            }
            else if (this == WORKERASSIGNED)
            {
                ret = "Builder has been hired and on their way";
            }
            else if (this == BLUEPRINT)
            {
                ret = "Builder is looking though blueprints";
            }
            else if (this == WAITINGFORRESOURCES)
            {
                ret = "Builder is checking the resources for the building";
            }
            else if (this == INPROGRESS)
            {
                ret = "Builder is busy building";
            }
            else if (this == COMPLETE)
            {
                ret = "Building work is complete";
            }

            return ret;
        }
    }
}
