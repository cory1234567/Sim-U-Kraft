package info.satscape.simukraft.common.jobs;

import info.satscape.simukraft.common.CommonProxy;
import info.satscape.simukraft.common.FolkData;
import info.satscape.simukraft.common.ModSimukraft;
import info.satscape.simukraft.common.CommonProxy.V3;
import info.satscape.simukraft.common.FolkData.FolkAction;
import info.satscape.simukraft.common.ModSimukraft.GameMode;
import info.satscape.simukraft.common.jobs.Job.Vocation;
import info.satscape.simukraft.common.jobs.JobBuilder.Stage;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.IInventory;

public class JobTerraformer extends Job
{
    public Vocation vocation = null;
    public FolkData theFolk = null;
    public Stage theStage;
    transient public int runDelay = 1000;
    transient public long timeSinceLastRun = 0;

    private transient TerraformerType theType;
    private transient int radius;
    private transient ArrayList<IInventory> constructorChests = new ArrayList<IInventory>();
    private transient int totalBlockCount = 0;
    private transient int counter = 0;
    private transient int buckets = 0;

    public JobTerraformer() {}

    public JobTerraformer(FolkData folk)
    {
        theFolk = folk;

        if (theStage == null)
        {
            theStage = Stage.IDLE;
        }

        if (theFolk == null)
        {
            return;   // is null when first employing, this is for next day(s)
        }

        if (theFolk.destination == null)
        {
            theFolk.gotoXYZ(theFolk.employedAt, null);
        }
    }

    public void resetJob()
    {
        theStage = Stage.IDLE;
        theFolk.isWorking = false;
    }

    public enum Stage
    {
        IDLE, WAITINGFORRESOURCES, INPROGRESS, COMPLETE;
    }
    public enum TerraformerType
    {
        WATERTODIRT, NATURE, LAWNMOWER, FLATTENIZER, VALUEPACK, GLACIAL, MOISTURIZER, THERMALIZER, DEICER;
    }
    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (!ModSimukraft.isDayTime())
        {
            theStage = Stage.IDLE;
        }

        super.onUpdateGoingToWork(theFolk);

        if (theStage == Stage.WAITINGFORRESOURCES)
        {
            runDelay = 4000;
        }

        if (theStage == Stage.INPROGRESS)
        {
            if (ModSimukraft.gameMode == GameMode.CREATIVE)
            {
                runDelay = 1;
            }
            else
            {
                runDelay = 300;
            }
        }

        if (System.currentTimeMillis() - timeSinceLastRun < runDelay)
        {
            return;
        }

        timeSinceLastRun = System.currentTimeMillis();

        // ////////////////IDLE
        if ((theStage == Stage.IDLE)
                && ModSimukraft.isDayTime())
        {
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

    private void stageWaitingForResources()
    {
        theFolk.isWorking = false;
        theFolk.statusText = "Checking terraforming resources...";
        constructorChests = inventoriesFindClosest(theFolk.employedAt, 5);

        if (step == 1)
        {
            if (constructorChests.isEmpty())
            {
                theFolk.statusText = "Please place at least ONE chest near to constructor box.";
            }
            else
            {
                theType = theFolk.terraformerType;
                radius = theFolk.terraformerRadius;

                if (theType == null)
                {
                    step = 4;
                }
                else
                {
                    step = 2;
                    constructorChests.get(0).openChest();
                }
            }
        }
        else if (step == 2)
        {
            constructorChests.get(0).closeChest();
            theStage = Stage.INPROGRESS;
            step = 1; //first start of inprogress
        }
        else if (step == 3)     // this step triggers mid-terraform - just send them
        {
            // back in to keep checking
            if (vocation == Vocation.TERRAFORMER)
            {
                step = 4;
                theStage = Stage.INPROGRESS;
            }
            else
            {
                theFolk.selfFire();
                return;
            }
        }
        else if (step == 4)
        {
            theFolk.statusText = "Please choose a Terraforming option";
            step = 1;
        }
    }

    private void stageInProgress()
    {
        theFolk.isWorking = true;
        Random rand = new Random();
        boolean hasPlacedTree = false;
        ItemStack is = null;
        constructorChests = inventoriesFindClosest(theFolk.employedAt, 5);

        if (step == 1)
        {
            if (theType == TerraformerType.WATERTODIRT)
            {
                ArrayList<Integer> blockIDs = new ArrayList<Integer>();
                blockIDs.add(Block.waterMoving.blockID);
                blockIDs.add(Block.waterStill.blockID);
                closestBlocks = null;
                setClosestBlocksOfType(theFolk.employedAt, blockIDs, radius, false, true, false);
            }
            else if (theType == TerraformerType.NATURE)
            {
                ArrayList<Integer> blockIDs = new ArrayList<Integer>();
                blockIDs.add(Block.dirt.blockID);
                blockIDs.add(Block.grass.blockID);
                closestBlocks = null;
                setClosestBlocksOfType(theFolk.employedAt, blockIDs, radius, true, true, false);
            }
            else if (theType == TerraformerType.LAWNMOWER)
            {
                ArrayList<Integer> blockIDs = new ArrayList<Integer>();
                blockIDs.add(Block.tallGrass.blockID);
                blockIDs.add(Block.plantRed.blockID);
                blockIDs.add(Block.plantYellow.blockID);
                closestBlocks = null;
                setClosestBlocksOfType(theFolk.employedAt, blockIDs, radius, false, true, false);
            }
            else if (theType == TerraformerType.FLATTENIZER)
            {
                ArrayList<Integer> blockIDs = new ArrayList<Integer>();
                blockIDs.add(Block.grass.blockID);
                blockIDs.add(Block.dirt.blockID);
                blockIDs.add(Block.tallGrass.blockID);
                blockIDs.add(Block.stone.blockID);
                blockIDs.add(Block.sand.blockID);
                blockIDs.add(Block.sandStone.blockID);
                blockIDs.add(Block.gravel.blockID);
                closestBlocks = null;
                setClosestBlocksOfType(theFolk.employedAt, blockIDs, radius, false, false, false);
            }
            else if (theType == TerraformerType.VALUEPACK)
            {
                ArrayList<Integer> blockIDs = new ArrayList<Integer>();
                blockIDs.add(0);
                blockIDs.add(Block.tallGrass.blockID);
                blockIDs.add(Block.plantRed.blockID);
                blockIDs.add(Block.plantYellow.blockID);
                V3 v = new V3(theFolk.employedAt.x, theFolk.employedAt.y - 1, theFolk.employedAt.z, theFolk.employedAt.theDimension);
                closestBlocks = null;
                setClosestBlocksOfType(v, blockIDs, radius, false, true, true);
            }
            else if (theType == TerraformerType.GLACIAL)
            {
                ArrayList<Integer> blockIDs = new ArrayList<Integer>();
                blockIDs.add(0);
                blockIDs.add(Block.tallGrass.blockID);
                blockIDs.add(Block.waterMoving.blockID);
                blockIDs.add(Block.waterStill.blockID);
                V3 v = new V3(theFolk.employedAt.x, theFolk.employedAt.y, theFolk.employedAt.z, theFolk.employedAt.theDimension);
                closestBlocks = null;
                setClosestBlocksOfType(v, blockIDs, radius, true, true, false);
            }
            else if (theType == TerraformerType.MOISTURIZER)
            {
                ArrayList<Integer> blockIDs = new ArrayList<Integer>();
                blockIDs.add(Block.lavaMoving.blockID);
                blockIDs.add(Block.lavaStill.blockID);
                //blockIDs.add(Block.waterStill.blockID);
                V3 v = new V3(theFolk.employedAt.x, theFolk.employedAt.y, theFolk.employedAt.z, theFolk.employedAt.theDimension);
                closestBlocks = null;
                setClosestBlocksOfType(v, blockIDs, radius, false, true, false);
            }
            else if (theType == TerraformerType.THERMALIZER)
            {
                ArrayList<Integer> blockIDs = new ArrayList<Integer>();
                blockIDs.add(Block.lavaStill.blockID);
                V3 v = new V3(theFolk.employedAt.x, theFolk.employedAt.y, theFolk.employedAt.z, theFolk.employedAt.theDimension);
                closestBlocks = null;
                setClosestBlocksOfType(v, blockIDs, radius, false, true, false);
            }
            else if (theType == TerraformerType.DEICER)
            {
                ArrayList<Integer> blockIDs = new ArrayList<Integer>();
                blockIDs.add(Block.snow.blockID);
                V3 v = new V3(theFolk.employedAt.x, theFolk.employedAt.y, theFolk.employedAt.z, theFolk.employedAt.theDimension);
                closestBlocks = null;
                setClosestBlocksOfType(v, blockIDs, radius, false, true, false);
            }

            step = 2;
        }
        else if (step == 2)
        {
            //searching in progress (thread)
            theFolk.statusText = "Scanning Terrain...";
        }
        else if (step == 3)
        {
            totalBlockCount = closestBlocks.size();

            if (totalBlockCount == 0)
            {
                theFolk.statusText = "Nothing to terraform!";
                ModSimukraft.sendChat("There's nothing here that can be terraformed in that way");
                theFolk.selfFire();
                return;
            }
            else
            {
                step = 4;
                theFolk.statusText = "Starting the terraforming process...";
            }
        }
        else if (step == 4)
        {
            int count = 0;

            if (theType == TerraformerType.WATERTODIRT)
            {
                if (ModSimukraft.gameMode != GameMode.CREATIVE)
                {
                    ItemStack gotDirt = inventoriesGet(constructorChests, new ItemStack(Block.dirt, 1), false,false,1);

                    if (gotDirt == null)
                    {
                        theFolk.statusText = "I need more dirt!";
                        theStage = Stage.WAITINGFORRESOURCES;
                        step = 1;
                        return;
                    }
                }
            }
            else if (theType == TerraformerType.NATURE)
            {
                counter++;
                hasPlacedTree = false;

                if (counter % 15 == 0)
                {
                    hasPlacedTree = true;

                    if (ModSimukraft.gameMode != GameMode.CREATIVE)
                    {
                        is = inventoriesGet(constructorChests, null, true,false,1);

                        if (is == null)
                        {
                            theFolk.statusText = "No more saplings, place some in a chest";
                            theStage = Stage.WAITINGFORRESOURCES;
                            step = 1;
                            return;
                        }
                    }
                    else
                    {
                        is = new ItemStack(Block.sapling, 1, new Random().nextInt(4));
                    }
                }
            }
            else if (theType == TerraformerType.LAWNMOWER || theType==TerraformerType.DEICER)
            {
                ///nothing needed
            }
            else if (theType == TerraformerType.FLATTENIZER)
            {
                ///nothing needed (only chest)
            }
            else if (theType == TerraformerType.VALUEPACK)
            {
                if (ModSimukraft.gameMode != GameMode.CREATIVE)
                {
                    ItemStack gotDirt = inventoriesGet(constructorChests, new ItemStack(Block.dirt, 1), false,false,1);

                    if (gotDirt == null)
                    {
                        theFolk.statusText = "I need more dirt!";
                        theStage = Stage.WAITINGFORRESOURCES;
                        step = 1;
                        return;
                    }
                }
            }
            else if (theType == TerraformerType.GLACIAL
                     || theType == TerraformerType.MOISTURIZER)
            {
                if (ModSimukraft.gameMode != GameMode.CREATIVE)
                {
                    if (buckets == 0)
                    {
                        ItemStack gotDirt = inventoriesGet(constructorChests, new ItemStack(Item.bucketWater, 1), false,false,1);

                        if (gotDirt == null)
                        {
                            theFolk.statusText = "Please place a bucket of water in the chest";
                            theStage = Stage.WAITINGFORRESOURCES;
                            step = 1;
                            return;
                        }
                        else
                        {
                            inventoriesPut(constructorChests, new ItemStack(Item.bucketEmpty, 1), true);
                            buckets = 1000;
                        }
                    }
                    else
                    {
                        buckets--;
                    }
                }
            }
            else if (theType == TerraformerType.THERMALIZER)
            {
                if (ModSimukraft.gameMode != GameMode.CREATIVE)
                {
                    ItemStack gotStuff = inventoriesGet(constructorChests, new ItemStack(Item.bucketEmpty, 1), false,false,1);

                    if (gotStuff == null)
                    {
                        theFolk.statusText = "I need some empty buckets to put the lava into.";
                        theStage = Stage.WAITINGFORRESOURCES;
                        step = 1;
                        return;
                    }
                }
            }

            //theFolk.swingProgress=rand.nextFloat();
            Double x, y;
            x = (double) totalBlockCount;
            y = (double) closestBlocks.size();
            Double percent = (double)(((x - y) /  x));
            percent = percent * 100;
            theFolk.statusText = "Terraforming, " + percent.intValue() + " % complete";
            V3 v = closestBlocks.get(0);

            if (theType == TerraformerType.WATERTODIRT)
            {
                jobWorld.setBlock(v.x.intValue(), v.y.intValue(), v.z.intValue(), Block.dirt.blockID, 0, 0x03);
                jobWorld.playAuxSFX(2001, v.x.intValue(), v.y.intValue(), v.z.intValue(), Block.dirt.blockID);

                if (ModSimukraft.gameMode != GameMode.CREATIVE)
                {
                    ModSimukraft.states.credits -= 0.009;
                }
            }
            else if (theType == TerraformerType.NATURE)
            {
                if (hasPlacedTree)
                {
                    jobWorld.setBlock(v.x.intValue(), v.y.intValue() + 1, v.z.intValue(),
                                      Block.sapling.blockID, is.getItemDamage(), 0x03);
                    jobWorld.playAuxSFX(2001, v.x.intValue(), v.y.intValue(), v.z.intValue(), Block.sapling.blockID);

                    if (ModSimukraft.gameMode != GameMode.CREATIVE)
                    {
                        ModSimukraft.states.credits -= 0.009;
                    }

                    runDelay = 500;
                }
                else
                {
                    int r = rand.nextInt(10);

                    if (r == 2)
                    {
                        jobWorld.setBlock(v.x.intValue(), v.y.intValue() + 1, v.z.intValue(), Block.plantRed.blockID, 0, 0x03);
                        jobWorld.markBlockForUpdate(v.x.intValue(), v.y.intValue() + 1, v.z.intValue());
                    }
                    else if (r == 5)
                    {
                        jobWorld.setBlock(v.x.intValue(), v.y.intValue() + 1, v.z.intValue(), Block.plantYellow.blockID, 0, 0x03);
                    }

                    runDelay = 50;
                }
            }
            else if (theType == TerraformerType.LAWNMOWER)
            {
                ArrayList<ItemStack> minedStacks = translateBlockWhenMined(jobWorld, v);

                if (minedStacks != null)
                {
                    for (int s = 0; s < minedStacks.size(); s++)
                    {
                        ItemStack stack = minedStacks.get(s);

                        if (stack != null)
                        {
                            inventoriesPut(constructorChests, stack, false);
                            jobWorld.playAuxSFX(2001, v.x.intValue(), v.y.intValue(), v.z.intValue(), stack.itemID);
                        }
                    }
                }

                if (mc.theWorld.isRemote)
                {
                    jobWorld.setBlock(v.x.intValue(), v.y.intValue(), v.z.intValue(), 0, 0, 0x03);
                    ModSimukraft.states.credits -= 0.009;
                }
            }
            else if (theType == TerraformerType.FLATTENIZER)
            {
                ArrayList<ItemStack> minedStacks = translateBlockWhenMined(jobWorld, v);

                if (minedStacks != null)
                {
                    for (int s = 0; s < minedStacks.size(); s++)
                    {
                        ItemStack stack = minedStacks.get(s);

                        if (stack != null)
                        {
                            inventoriesPut(constructorChests, stack, false);
                            jobWorld.playAuxSFX(2001, v.x.intValue(), v.y.intValue(), v.z.intValue(), stack.itemID);
                        }
                    }
                }

                try
                {
                    if (mc.theWorld.isRemote)
                    {
                        jobWorld.setBlock(v.x.intValue(), v.y.intValue(), v.z.intValue(), 0, 0, 0x03);

                        if (ModSimukraft.gameMode != GameMode.CREATIVE)
                        {
                            ModSimukraft.states.credits -= 0.009;
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if (theType == TerraformerType.VALUEPACK)
            {
                if (mc.theWorld.isRemote)
                {
                    jobWorld.setBlock(v.x.intValue(), v.y.intValue(), v.z.intValue(), Block.dirt.blockID, 0, 0x03);
                    jobWorld.playAuxSFX(2001, v.x.intValue(), v.y.intValue(), v.z.intValue(), Block.dirt.blockID);

                    if (ModSimukraft.gameMode != GameMode.CREATIVE)
                    {
                        ModSimukraft.states.credits -= 0.009;
                    }
                }
            }
            else if (theType == TerraformerType.GLACIAL)
            {
                int blockId = jobWorld.getBlockId(v.x.intValue(), v.y.intValue(), v.z.intValue());

                if (blockId == 0 || blockId == Block.tallGrass.blockID)
                {
                    int idBelow = jobWorld.getBlockId(v.x.intValue(), v.y.intValue() - 1, v.z.intValue());

                    if (idBelow > 0 && idBelow != Block.ice.blockID &&
                            idBelow != Block.waterMoving.blockID &&
                            idBelow != Block.waterStill.blockID &&
                            idBelow != Block.snow.blockID)  //non-air block below (some type of ground
                    {
                        if (mc.theWorld.isRemote)
                        {
                            jobWorld.setBlock(v.x.intValue(), v.y.intValue(), v.z.intValue(), Block.snow.blockID, 0, 0x03);
                            jobWorld.playAuxSFX(2001, v.x.intValue(), v.y.intValue(), v.z.intValue(), Block.snow.blockID);

                            if (ModSimukraft.gameMode != GameMode.CREATIVE)
                            {
                                ModSimukraft.states.credits -= 0.009;
                            }
                        }
                    }
                }
                else if (blockId == Block.waterMoving.blockID || blockId == Block.waterStill.blockID)
                {
                    if (mc.theWorld.isRemote)
                    {
                        jobWorld.setBlock(v.x.intValue(), v.y.intValue(), v.z.intValue(), Block.ice.blockID, 0, 0x03);
                        jobWorld.playAuxSFX(2001, v.x.intValue(), v.y.intValue(), v.z.intValue(), Block.ice.blockID);

                        if (ModSimukraft.gameMode != GameMode.CREATIVE)
                        {
                            ModSimukraft.states.credits -= 0.009;
                        }
                    }
                }
            }
            else if (theType == TerraformerType.MOISTURIZER)
            {
                if (mc.theWorld.isRemote)
                {
                    jobWorld.setBlock(v.x.intValue(), v.y.intValue(), v.z.intValue(), Block.obsidian.blockID, 0, 0x03);
                    jobWorld.playAuxSFX(2001, v.x.intValue(), v.y.intValue(), v.z.intValue(), Block.waterMoving.blockID);
                    jobWorld.markBlockForUpdate(v.x.intValue(), v.y.intValue(), v.z.intValue());

                    if (ModSimukraft.gameMode != GameMode.CREATIVE)
                    {
                        ModSimukraft.states.credits -= 0.009;
                    }
                }
            }
            else if (theType == TerraformerType.THERMALIZER)
            {
                if (mc.theWorld.isRemote)
                {
                    jobWorld.setBlock(v.x.intValue(), v.y.intValue(), v.z.intValue(), 0, 0, 0x03);
                    jobWorld.markBlockForUpdate(v.x.intValue(), v.y.intValue(), v.z.intValue());

                    if (ModSimukraft.gameMode != GameMode.CREATIVE)
                    {
                        ModSimukraft.states.credits -= 0.009;
                    }

                    inventoriesPut(constructorChests, new ItemStack(Item.bucketLava, 1), false);
                }
            }
            else if (theType == TerraformerType.DEICER)
            {
                if (mc.theWorld.isRemote)
                {
                    jobWorld.setBlock(v.x.intValue(), v.y.intValue(), v.z.intValue(), 0, 0, 0x03);
                    jobWorld.markBlockForUpdate(v.x.intValue(), v.y.intValue(), v.z.intValue());
                    counter++;
                    
                    if (ModSimukraft.gameMode != GameMode.CREATIVE)
                    {
                        ModSimukraft.states.credits -= 0.009;
                    }
                    
                    if (counter % 4==0) {
                    	inventoriesPut(constructorChests, new ItemStack(Block.blockSnow, 1,0), false);
                    }
                }
            }
            

            closestBlocks.remove(0);

            if (closestBlocks.size() == 0)
            {
                theStage = Stage.COMPLETE;
            }
        }
    }

    private void stageComplete()
    {
        theFolk.isWorking = false;
        ModSimukraft.sendChat(theFolk.name
                              + " has completed terraforming");
        mc.theWorld.playSound(mc.thePlayer.posX, mc.thePlayer.posY,
                              mc.thePlayer.posZ, "satscapesimukraft:cash", 1f, 1f, false);
        theFolk.stayPut = false;
        theFolk.terraformerRadius = 1;
        theFolk.terraformerType = null;
        theFolk.selfFire();
        theStage = Stage.IDLE;
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
            theFolk.statusText = "Arrived at the site";
            theStage = Stage.WAITINGFORRESOURCES;
        }
        else
        {
            theFolk.gotoXYZ(theFolk.employedAt, null);
        }
    }
}
