package info.satscape.simukraft.common.jobs;

import info.satscape.simukraft.common.CommonProxy;
import info.satscape.simukraft.common.FarmingBox;
import info.satscape.simukraft.common.FolkData;
import info.satscape.simukraft.common.ModSimukraft;
import info.satscape.simukraft.common.CommonProxy.V3;
import info.satscape.simukraft.common.FarmingBox.FarmType;
import info.satscape.simukraft.common.FolkData.FolkAction;
import info.satscape.simukraft.common.FolkData.GotoMethod;
import info.satscape.simukraft.common.ModSimukraft.GameMode;
import info.satscape.simukraft.common.jobs.Job.Vocation;
import info.satscape.simukraft.common.jobs.JobMiner.Stage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

public class JobCropFarmer extends Job implements Serializable
{
    private static final long serialVersionUID = -1177112214234279141L;

    public Vocation vocation = null;
    public FolkData theFolk = null;
    public Stage theStage;
    transient public int runDelay = 1000;
    transient public long timeSinceLastRun = 0;

    private transient boolean doneSomeWork = false;
    private transient FarmingBox farmingBlock = null;
    private transient ArrayList<IInventory> farmingChests = new ArrayList<IInventory>();
    private transient String farmDir = "";
    private transient int ftbCount = 0, ltrCount = 0;
    private transient int xo = 0, zo = 0;
    private transient int id = 0;
    private transient int mx, my, mz;
    private transient int xxx = 0, yyy = 0, zzz = 0;
    private transient int ftb = 1, ltr = -1;
    private transient int meta = 0;
    private transient long lastFarmCycle = 0l;
    private transient long lastCustomHarvest = 0l;
    private transient int rowCounter = 0;

    public JobCropFarmer()
    {
    }

    public JobCropFarmer(FolkData folk)
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

        farmingBlock = FarmingBox.getFarmingBlockByBoxXYZ(folk.employedAt);
        runDelay = 1000;
    }

    public void resetJob()
    {
        theStage = Stage.IDLE;
        theFolk.isWorking = false;
    }

    public enum Stage
    {
        IDLE, ARRIVEDATFARM, CHECKINGFORCHESTS, HOELAND, PLANTSEEDS, HARVEST, HANGOUT;
    }

    @Override
    public void onUpdate()
    {
        if (ModSimukraft.theFarmingBoxes.size() == 0)
        {
            return;
        } // they haven't loaded yet, so fuck off

        super.onUpdate();

        if (!ModSimukraft.isDayTime())
        {
            theStage = Stage.IDLE;
        }

        super.onUpdateGoingToWork(theFolk);

        if (theStage == Stage.CHECKINGFORCHESTS)
        {
            runDelay = 1000;
        }

        if (theStage == Stage.HARVEST || theStage == Stage.HOELAND
                || theStage == Stage.PLANTSEEDS)
        {
            runDelay = 500;
        }

        if (theStage == Stage.HANGOUT)
        {
            if (step == 1)
            {
                runDelay = 1000;
            }
            else
            {
                runDelay = 60000;
            }
        }

        if (System.currentTimeMillis() - timeSinceLastRun < runDelay)
        {
            return;
        }

        timeSinceLastRun = System.currentTimeMillis();

        // ////////////////IDLE
        if (theStage == Stage.IDLE && ModSimukraft.isDayTime())
        {
        }
        else if (theStage == Stage.ARRIVEDATFARM)
        {
            theStage = Stage.CHECKINGFORCHESTS;
        }
        else if (theStage == Stage.CHECKINGFORCHESTS)
        {
            stageCheckingForChests();
        }
        else if (theStage == Stage.HARVEST)
        {
            stageHarvest();
        }
        else if (theStage == Stage.HOELAND)
        {
            stageHoeland();
        }
        else if (theStage == Stage.PLANTSEEDS)
        {
            stagePlantSeeds();
        }
        else if (theStage == Stage.HANGOUT)
        {
            stageHangout();
        }
    }

    public void stageCheckingForChests()
    {
        if (farmingChests.isEmpty())
        {
            farmingChests = inventoriesFindClosest(theFolk.employedAt, 5);
        }

        ModSimukraft.log.info("JobCropFarmer: found " + farmingChests.size()
                         + " chests at the farm");
        theFolk.stayPut = true;
        int dist = theFolk.location.getDistanceTo(theFolk.employedAt);

        if (dist > 3)
        {
            theFolk.gotoXYZ(theFolk.employedAt, null);
        }

        if (farmingChests.isEmpty())
        {
            theFolk.statusText = "Please place at least one chest near the farming box";
        }
        else
        {
            theStage = Stage.HARVEST;
            step = 1;
            theFolk.stayPut = true;

            if (theFolk.gender == 0)
            {
                jobWorld.playSound(theFolk.location.x, theFolk.location.y,
                                   theFolk.location.z, "satscapesimukraft:readym", 1f, 1f,
                                   false);
            }
            else
            {
                jobWorld.playSound(theFolk.location.x, theFolk.location.y,
                                   theFolk.location.z, "satscapesimukraft:readyf", 1f, 1f,
                                   false);
            }
        }
    }

    /** sets the farmDir variable and ftb and ltr values */
    private void setupFarming()
    {
        ftb = 0;
        ltr = -1;

        if (farmingBlock == null)
        {
            ModSimukraft.log.warning("JobCropFarmer: FarmingBlock is null - not there or not found?!");
            return;
        }

        V3 m1 = farmingBlock.marker1XYZ;
        V3 m2 = farmingBlock.marker2XYZ;
        V3 m3 = farmingBlock.marker3XYZ;

        if (farmingBlock.marker1XYZ == null)
        {
            ModSimukraft.log.warning("JobCropFarmer: FarmingBlock's markers are null");
            return;
        }

        try
        {
            // the ground below the first marker
            mx = m1.x.intValue();
            my = m1.y.intValue() - 1;
            mz = m1.z.intValue();
            int m2x = m2.x.intValue();
            int m1x = m1.x.intValue();
            int m2z = m2.z.intValue();
            int m1z = m1.z.intValue();

            if (m2x == m1x)
            {
                if (m2z > mz)
                {
                    farmDir = "z+";
                }
                else
                {
                    farmDir = "z-";
                }
            }
            else if (m2z == m1z)
            {
                if (m2x > mx)
                {
                    farmDir = "x+";
                }
                else
                {
                    farmDir = "x-";
                }
            }

            ltrCount = farmingBlock.getSizeWidth();
            ftbCount = farmingBlock.getSizeLength();
        }
        catch (Exception e)
        {

        }

    }

    /**
     * sets the xxx,yyy and zzz for the next farming block @return true if we're
     * done
     */
    private boolean setXYZ()
    {
        boolean ret = false;
        ltr++;

        if (ltr > (ltrCount + 1))
        {
            ltr = 0;
            ftb++;

            if (ftb > (ftbCount + 1))
            {
                ret = true;
            }
        }

        if (farmDir.contentEquals("x+"))
        {
            xo = ltr;
            zo = -ftb;
        }
        else if (farmDir.contentEquals("x-"))
        {
            xo = -ltr;
            zo = ftb;
        }
        else if (farmDir.contentEquals("z+"))
        {
            xo = ftb;
            zo = ltr;
        }
        else if (farmDir.contentEquals("z-"))
        {
            xo = -ftb;
            zo = -ltr;
        }

        try
        {
            xxx = mx + xo;
            yyy = farmingBlock.location.y.intValue(); // Y of the ground below
            // the farming box
            zzz = mz + zo;
        }
        catch (Exception e)
        {
            ModSimukraft.sendChat("There was a problem with " + theFolk.name
                                  + "'s farming box, please replace it");
            theFolk.selfFire();
            return false;
        }

        return ret;
    }

    public void stageHarvest()
    {
        if(farmingBlock==null || farmingBlock.farmType==null) {
        	ModSimukraft.sendChat("There's a problem with a farming block, please re-make it");
        	if(theFolk !=null) {
        		theFolk.selfFire();
        	}
        }
    	
    	if (step == 1)
        {
            setupFarming();
            theFolk.statusText = "Harvesting";
            int dist = theFolk.location.getDistanceTo(theFolk.employedAt);

            if (dist > 3)
            {
                theFolk.gotoXYZ(theFolk.employedAt, null);
            }

            step = 2;
            theFolk.isWorking = true;
        }
        else if (step == 2)
        {
            boolean done = setXYZ();
            boolean hasHarvest = false;

            if (!done)
            {
                while (!hasHarvest)
                {
                    id = jobWorld.getBlockId(xxx, yyy, zzz);
                    meta = jobWorld.getBlockMetadata(xxx, yyy, zzz);

                    //artificially grow a non-custom/sugar farm when chunk in not loaded
                    try
                    {
                        if (!theFolk.isSpawned() && farmingBlock.farmType != FarmType.CUSTOM
                                && farmingBlock.farmType != FarmType.SUGAR && farmingBlock.farmType != FarmType.CACTUS)
                        {
                            if (meta < 7)
                            {
                                meta++;
                                jobWorld.setBlock(xxx, yyy, zzz, id, meta, 0x03);
                            }
                        }
                    }
                    catch (Exception e) {}  //farmingBlock can be null if block removed, but farmer not fired

                    boolean canHarvest = false;
                    V3 harvestBlock = new V3((double) xxx, (double) yyy, (double) zzz, jobWorld.provider.dimensionId);
                    ArrayList<ItemStack> minedStacks = translateBlockWhenMined(jobWorld, harvestBlock);

                    //sugar cane/Cactus farms
                    if (farmingBlock.farmType == FarmType.SUGAR || farmingBlock.farmType == FarmType.CACTUS)
                    {
                        int sid1 = jobWorld.getBlockId(xxx, yyy + 1, zzz);
                        int sid2 = jobWorld.getBlockId(xxx, yyy + 2, zzz);

                        if (sid1 == Block.reed.blockID && sid2 == Block.reed.blockID)
                        {
                            canHarvest = true;
                        }

                        if (sid1 == Block.cactus.blockID && sid2 == Block.cactus.blockID)
                        {
                            canHarvest = true;
                        }

                        // all other types of farm
                    }
                    else if (id == Block.melon.blockID || id == Block.pumpkin.blockID
                             || farmingBlock.farmType == FarmType.CUSTOM
                             || meta >= 7)
                    {
                        if (id != Block.pumpkinStem.blockID && id != Block.melonStem.blockID)
                        {
                            canHarvest = true;
                        }

                        if (id == 0)
                        {
                            canHarvest = false; //override above code when custom farms have nothing planted there or partial farm
                        }
                    }
                    else
                    {
                      
                    }

                    if (canHarvest)
                    {
                        if (farmingBlock.farmType == FarmType.SUGAR || farmingBlock.farmType == FarmType.CACTUS)
                        {
                            this.farmingChests = inventoriesFindClosest(theFolk.employedAt, 5);
                            jobWorld.setBlock(xxx, yyy + 1, zzz, 0, 0, 0x03);
                            jobWorld.setBlock(xxx, yyy + 2, zzz, 0, 0, 0x03);

                            if (farmingBlock.farmType == FarmType.SUGAR)
                            {
                                jobWorld.playAuxSFX(2001, xxx, yyy + 1, zzz, Block.reed.blockID);
                                inventoriesPut(farmingChests, new ItemStack(Item.reed, 2), false);
                            }
                            else if (farmingBlock.farmType == FarmType.CACTUS)
                            {
                                jobWorld.playAuxSFX(2001, xxx, yyy + 1, zzz, Block.cactus.blockID);
                                inventoriesPut(farmingChests, new ItemStack(Block.cactus, 2), false);
                            }
                        }
                        else if (farmingBlock.farmType != FarmType.CUSTOM)
                        {
                            if (minedStacks != null)
                            {
                                this.farmingChests = inventoriesFindClosest(theFolk.employedAt, 5);

                                for (int s = 0; s < minedStacks.size(); s++)
                                {
                                    ItemStack stack = minedStacks.get(s);

                                    if (stack != null)
                                    {
                                        inventoriesPut(farmingChests, stack, false);

                                    }
                                }
                            }
                            else
                            {

                            }

                            jobWorld.setBlock(xxx, yyy, zzz, 0, 0, 0x03);
                        }
                        else      // CUSTOM FARM ONLY
                        {
                            if (System.currentTimeMillis() - lastCustomHarvest < (60 * 60 * 1000))
                            {
                                theStage = Stage.HOELAND;
                                step = 1;
                                theFolk.isWorking = false;
                                return;
                            }
                            else
                            {
                                jobWorld.destroyBlock(xxx, yyy, zzz, true);
                                pickUpDroppedCrops(harvestBlock);
                            }
                        }

                        if (farmingBlock.farmType != FarmType.CUSTOM)
                        {
                            jobWorld.playAuxSFX(2001, xxx, yyy, zzz, id + (meta << 12));
                        }

                        ModSimukraft.states.credits -= 0.02f;
                        doneSomeWork = true;
                        hasHarvest = true;
                    }
                    else
                    {
                        hasHarvest = false;
                        done = setXYZ();

                        if (done)
                        {
                            theStage = Stage.HOELAND;
                            step = 1;
                            theFolk.isWorking = false;
                            return;
                        }
                    }
                }
            }
            else     // we're done harvesting
            {
                theStage = Stage.HOELAND;
                step = 1;
                theFolk.isWorking = false;
                lastCustomHarvest = System.currentTimeMillis();
                return;
            }
        } // end of step 2
    }

    /* custom farms breaks the block, so this is called to pick up the drop */
    private void pickUpDroppedCrops(V3 v3center)
    {
        if (theFolk.theEntity == null)
        {
            return;   //can't do this if the entity is de-spawned!
        }

        List list1 = jobWorld.getEntitiesWithinAABBExcludingEntity(
                         theFolk.theEntity, AxisAlignedBB.getBoundingBox(v3center.x, v3center.y, v3center.z, v3center.x + 1.0D,
                                 v3center.y + 1.0D, v3center.z + 1.0D).expand(3D, 2D, 3D));
        Iterator iterator1 = list1.iterator();

        if (!list1.isEmpty())
        {
            do
            {
                if (!iterator1.hasNext())
                {
                    break;
                }

                Entity entity1 = (Entity) iterator1.next();

                if (!(entity1 instanceof EntityItem))
                {
                    continue;
                }

                EntityItem entityitem = (EntityItem) entity1;
                ItemStack is = entityitem.getEntityItem();

                try
                {
                    ItemFood food = (ItemFood) is.getItem();

                    if (food != null)
                    {
                        boolean ok = inventoriesPut(farmingChests, is, false);

                        if (ok)
                        {
                            entityitem.setDead();
                        }
                    }
                }
                catch (Exception e) {}  //Cast Exception when not food
            }
            while (true);
        }
    }

    public void stageHoeland()
    {
        if (step == 1)
        {
            setupFarming();
            theFolk.statusText = "Tilling the land";
            theFolk.stayPut = true;
            theFolk.action = FolkAction.ATWORK;
            step = 2;
            theFolk.isWorking = true;
            rowCounter = 0;
        }
        else if (step == 2)
        {
            boolean done = false;
            boolean hasTilled = false;

            while (!hasTilled && !done)
            {
                done = setXYZ();

                if (done)
                {
                    theStage = Stage.PLANTSEEDS;
                    step = 1;
                    return;
                }

                id = jobWorld.getBlockId(xxx, yyy - 1, zzz);
                meta = mc.theWorld.getBlockMetadata(xxx, yyy - 1, zzz);

                //// SUGAR CANE
                if (farmingBlock.farmType == FarmType.SUGAR)
                {
                    theFolk.statusText = "preparing the land";

                    /// dirt water dirt   0 1 2   3 4 5   6 7 8    9 10 11
                    if (rowCounter % 3 == 0 || (rowCounter + 1) % 3 == 0)
                    {
                        if (id != Block.dirt.blockID && id != Block.grass.blockID)
                        {
                            jobWorld.setBlock(xxx, yyy - 1, zzz, Block.dirt.blockID, 0, 0x03);
                            jobWorld.playSound(xxx, yyy - 1, zzz,
                                               Block.grass.stepSound.getStepSound(), 1.0f,
                                               1.0f, false);
                            hasTilled = true;
                            ModSimukraft.states.credits -= 0.01f;
                        }
                    }
                    else if ((rowCounter + 2) % 3 == 0)
                    {
                        if (id != Block.waterStill.blockID)
                        {
                            jobWorld.setBlock(xxx, yyy - 1, zzz, Block.waterStill.blockID, 0, 0x03);
                            hasTilled = true;
                            ModSimukraft.states.credits -= 0.01f;
                        }
                    }

                    rowCounter++;

                    if (rowCounter > farmingBlock.getSizeWidth() + 1)
                    {
                        rowCounter = 0;
                    }
                }
                else if (farmingBlock.farmType == FarmType.CACTUS)
                {
                    theFolk.statusText = "preparing the land";

                    if ((xxx + zzz) % 2 == 0)
                    {
                        if (id != Block.sand.blockID)
                        {
                            jobWorld.setBlock(xxx, yyy - 1, zzz, Block.sand.blockID, 0, 0x03);
                            hasTilled = true;
                            ModSimukraft.states.credits -= 0.01f;
                        }
                    }

                    //// ALL OTHER FARMS
                }
                else
                {
                    if (id == Block.grass.blockID || id == Block.dirt.blockID)
                    {
                        if (((farmingBlock.farmType == FarmType.MELON || farmingBlock.farmType == FarmType.PUMPKIN) && (ftb % 4 == 0 || ftb % 4 == 1))
                                || farmingBlock.farmType == FarmType.WHEAT
                                || farmingBlock.farmType == FarmType.CARROT
                                || farmingBlock.farmType == FarmType.POTATO
                                || farmingBlock.farmType == FarmType.CUSTOM)
                        {
                            jobWorld.setBlock(xxx, yyy - 1, zzz,
                                              Block.tilledField.blockID, 0, 0x03);
                            jobWorld.playSound(xxx, yyy - 1, zzz,
                                               Block.grass.stepSound.getStepSound(), 1.0f,
                                               1.0f, false);
                            hasTilled = true;
                            ModSimukraft.states.credits -= 0.01f;
                        }
                    }
                }

                if (done)
                {
                    theStage = Stage.PLANTSEEDS;
                    step = 1;
                    theFolk.isWorking = false;
                    return;
                }
            }
        }
    }

    /**
     *
     */
    public void stagePlantSeeds()
    {
        if (step == 1)
        {
            setupFarming();
            theFolk.stayPut = true;
            theFolk.action = FolkAction.ATWORK;
            step = 2;
            theFolk.isWorking = true;
        }
        else if (step == 2)
        {
            boolean done = false;
            boolean hasSown = false;

            while (!hasSown && !done)
            {
                done = setXYZ();

                if (done)
                {
                    theStage = Stage.HANGOUT;
                    theFolk.statusText = "Relaxing at the farm";
                    step = 1;
                    return;
                }

                int gid = jobWorld.getBlockId(xxx, yyy - 1, zzz);
                int aid = jobWorld.getBlockId(xxx, yyy, zzz);

                if ((gid == Block.sand.blockID || gid == Block.grass.blockID
                        || gid == Block.dirt.blockID || gid == Block.tilledField.blockID)
                        && aid == 0)
                {
                    try
                    {
                        if (farmingBlock.farmType != FarmType.CUSTOM)
                        {
                            theFolk.statusText = "Planting "
                                                 + farmingBlock.farmType.toString()
                                                 + " seeds";
                        }
                    }
                    catch (Exception e)
                    {
       
                    }

                    if (farmingBlock.farmType == FarmType.WHEAT)
                    {
                        if (ModSimukraft.gameMode != GameMode.CREATIVE)
                        {
                            ItemStack seed = inventoriesGet(farmingChests,
                                                            new ItemStack(Item.seeds, 1), false,false,-1);

                            if (seed == null)
                            {
                                theFolk.statusText = "No more wheat seeds to plant";
                                theStage = Stage.HANGOUT;
                                step = 1;
                                return;
                            }
                        }

                        jobWorld.setBlock(xxx, yyy - 1, zzz,
                                          Block.tilledField.blockID, 0, 0x03);
                        jobWorld.setBlock(xxx, yyy, zzz, Block.crops.blockID,
                                          0, 0x03);
                        hasSown = true;
                    }
                    else if (farmingBlock.farmType == FarmType.PUMPKIN)
                    {
                        if (ftb % 4 == 0 || ftb % 4 == 1)   // even rows only to
                        {
                            // leave room for
                            // fruit
                            if (ModSimukraft.gameMode != GameMode.CREATIVE)
                            {
                                ItemStack seed = inventoriesGet(farmingChests,
                                                                new ItemStack(Item.pumpkinSeeds, 1),
                                                                false,false,-1);

                                if (seed == null)
                                {
                                    theFolk.statusText = "I need more pumpkin seeds!";
                                    theStage = Stage.HANGOUT;
                                    step = 1;
                                    return;
                                }
                            }

                            jobWorld.setBlock(xxx, yyy - 1, zzz,
                                              Block.tilledField.blockID, 0, 0x03);
                            jobWorld.setBlock(xxx, yyy, zzz,
                                              Block.pumpkinStem.blockID, 0, 0x03);
                            hasSown = true;
                        }
                    }
                    else if (farmingBlock.farmType == FarmType.MELON)
                    {
                        if (ftb % 4 == 0 || ftb % 4 == 1)
                        {
                            if (ModSimukraft.gameMode != GameMode.CREATIVE)
                            {
                                ItemStack seed = inventoriesGet(farmingChests,
                                                                new ItemStack(Item.melonSeeds, 1),
                                                                false,false,-1);

                                if (seed == null)
                                {
                                    theFolk.statusText = "I need more melon seeds!";
                                    theStage = Stage.HANGOUT;
                                    step = 1;
                                    return;
                                }
                            }

                            jobWorld.setBlock(xxx, yyy - 1, zzz,
                                              Block.tilledField.blockID, 0, 0x03);
                            jobWorld.setBlock(xxx, yyy, zzz,
                                              Block.melonStem.blockID, 0, 0x03);
                            hasSown = true;
                        }
                    }
                    else if (farmingBlock.farmType == FarmType.CARROT)
                    {
                        if (ModSimukraft.gameMode != GameMode.CREATIVE)
                        {
                            ItemStack seed = inventoriesGet(farmingChests,
                                                            new ItemStack(Item.carrot, 1), false,false,-1);

                            if (seed == null)
                            {
                                theFolk.statusText = "I need more carrots to plant!";
                                theStage = Stage.HANGOUT;
                                step = 1;
                                return;
                            }
                        }

                        jobWorld.setBlock(xxx, yyy - 1, zzz,
                                          Block.tilledField.blockID, 0, 0x03);
                        jobWorld.setBlock(xxx, yyy, zzz, Block.carrot.blockID,
                                          0, 0x03);
                        hasSown = true;
                    }
                    else if (farmingBlock.farmType == FarmType.POTATO)
                    {
                        if (ModSimukraft.gameMode != GameMode.CREATIVE)
                        {
                            ItemStack seed = inventoriesGet(farmingChests,
                                                            new ItemStack(Item.potato, 1), false,false,-1);

                            if (seed == null)
                            {
                                theFolk.statusText = "I need more potatoes to plant!";
                                theStage = Stage.HANGOUT;
                                step = 1;
                                return;
                            }
                        }

                        jobWorld.setBlock(xxx, yyy - 1, zzz,
                                          Block.tilledField.blockID, 0, 0x03);
                        jobWorld.setBlock(xxx, yyy, zzz, Block.potato.blockID,
                                          0, 0x03);
                        hasSown = true;
                    }
                    else if (farmingBlock.farmType == FarmType.SUGAR)
                    {
                        int cid = jobWorld.getBlockId(xxx, yyy - 1, zzz);

                        if (cid == Block.dirt.blockID || cid == Block.grass.blockID || cid == Block.sand.blockID)
                        {
                            if (ModSimukraft.gameMode != GameMode.CREATIVE)
                            {
                                ItemStack seed = inventoriesGet(farmingChests,
                                                                new ItemStack(Item.reed, 1), false,false,-1);

                                if (seed == null)
                                {
                                    theFolk.statusText = "No more sugar cane to plant";
                                    theStage = Stage.HANGOUT;
                                    step = 1;
                                    return;
                                }
                            }

                            jobWorld.setBlock(xxx, yyy, zzz, Block.reed.blockID,
                                              0, 0x03);
                            hasSown = true;
                        }
                    }
                    else if (farmingBlock.farmType == FarmType.CACTUS)
                    {
                        if ((xxx + zzz) % 2 == 0)
                        {
                            if (ModSimukraft.gameMode != GameMode.CREATIVE)
                            {
                                ItemStack seed = inventoriesGet(farmingChests,
                                                                new ItemStack(Block.cactus, 1), false,false,-1);

                                if (seed == null)
                                {
                                    theFolk.statusText = "No more cactus to plant";
                                    theStage = Stage.HANGOUT;
                                    step = 1;
                                    return;
                                }
                            }

                            jobWorld.setBlock(xxx, yyy, zzz, Block.cactus.blockID, 0, 0x03);
                            hasSown = true;
                        }
                    }
                    else if (farmingBlock.farmType == FarmType.CUSTOM)
                    {
                        fuckOff:

                        for (int ch = 0; ch < farmingChests.size(); ch++)
                        {
                            IInventory chest = farmingChests.get(ch);

                            for (int g = 0; g < chest.getSizeInventory(); g++)
                            {
                                ItemStack chestStack = chest.getStackInSlot(g);

                                if (chestStack != null)  // && chestStack.getItem() instanceof IPlantable) {
                                {
                                    theFolk.statusText = "Planting "
                                                         + chestStack.getDisplayName();
                                    ItemStack seed = inventoriesGet(
                                                         farmingChests, new ItemStack(
                                                             chestStack.getItem(), 1),
                                                         false,false,-1);

                                    if (seed != null)
                                    {
                                        jobWorld.setBlock(xxx, yyy - 1, zzz,
                                                          Block.tilledField.blockID, 0,
                                                          0x03);
                                        hasSown = seed.getItem().onItemUse(seed, mc.thePlayer, jobWorld, xxx, yyy - 1, zzz, 1, 0, 0, 0);

                                        if (!hasSown)
                                        {
                                            theFolk.inventory.add(seed);
                                            
                                        }

                                        break fuckOff;
                                    }
                                }
                            }
                        } // next chest
                    }

                    if (hasSown)
                    {
                        jobWorld.playSound(xxx, yyy, zzz,
                                           Block.grass.stepSound.getStepSound(), 1.0f,
                                           1.0f, false);
                        ModSimukraft.states.credits -= 0.01f;
                        doneSomeWork = true;
                    }
                }
            }

            if (done)
            {
                theStage = Stage.HANGOUT;
                theFolk.statusText = "Relaxing at the farm";
                step = 1;
                theFolk.isWorking = false;

                this.inventoriesTransferFromFolk(theFolk.inventory, this.farmingChests, null);
                return;
            }
        }
    }

    public void stageHangout()
    {
        if (step == 1)
        {
            lastFarmCycle = System.currentTimeMillis();
            step = 2;
            theFolk.isWorking = false;
        }
        else if (step == 2)
        {
            Random ra = new Random();
            int dist = theFolk.location.getDistanceTo(theFolk.employedAt);

            if (dist > 3)
            {
                theFolk.gotoXYZ(theFolk.employedAt, null);
            }

            int r = ra.nextInt(10);

            if (r == 0)
            {
                if (ModSimukraft.gameMode == GameMode.HARDCORE)
                {
                    theFolk.statusText = "Wow, Hardcore mode is really hard!";
                }
                else
                {
                    theFolk.statusText = "Posting a picture of my farm on Facebook";
                }
            }
            else if (r == 1)
            {
                theFolk.statusText = "Checking the weather forecast";
            }
            else if (r == 2)
            {
                theFolk.statusText = "Wishing I had a tractor";
            }
            else if (r == 3)
            {
                theFolk.statusText = "Having a break";
            }
            else if (r == 4)
            {
                theFolk.statusText = "Cleaning dirt off my Hoe";
            }
            else if (r == 5)
            {
                theFolk.statusText = "Sharpening my Hoe";
            }
            else if (r == 6)
            {
                theFolk.statusText = "Eating my lunch";
            }
            else if (r == 7)
            {
                theFolk.statusText = "Reticulating my splines";
            }
            else if (r == 8)
            {
                theFolk.statusText = "Relaxing for a while";
            }
            else if (r == 9)
            {
                theFolk.statusText = "Wishing I was playing Minecraft";
            }

            if (System.currentTimeMillis() - lastFarmCycle > (3 * 60 * 1000))
            {
                theStage = Stage.HARVEST;
                step = 1;
                return;
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
            theFolk.statusText = "Arrived on the farm";
            theStage = Stage.ARRIVEDATFARM;
        }
        else
        {
            theFolk.gotoXYZ(theFolk.employedAt, null);
        }
    }
}
