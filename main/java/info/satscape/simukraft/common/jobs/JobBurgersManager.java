package info.satscape.simukraft.common.jobs;

import info.satscape.simukraft.common.Building;
import info.satscape.simukraft.common.FolkData;
import info.satscape.simukraft.common.ModSimukraft;
import info.satscape.simukraft.common.CommonProxy.V3;
import info.satscape.simukraft.common.FolkData.FolkAction;
import info.satscape.simukraft.common.FolkData.GotoMethod;

import java.util.ArrayList;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class JobBurgersManager extends Job {
	
	
	/*
	Specialblock meta values for this job:
	0=Raw materials chest waypoint
	1=Frycook waypoint
	2=Waiter waypoint
	*/
	public Vocation vocation = null;
    public FolkData theFolk = null;
    public Stage theStage;
    public int runDelay = 1000;
    private long timeSinceLastRun=0;
    private Building theStore=null;
    private int currentPickup=0;
    private ArrayList<Building> pickupBuildings=new ArrayList<Building>();
    
    
	public JobBurgersManager(FolkData folk) {
      theFolk = folk;

        if (theStage == null)
        {
            theStage = Stage.IDLE;
        }

        if (theFolk == null)
        {
            return;   // is null when first employing
        }

        if (theFolk.destination == null)
        {
            theFolk.gotoXYZ(theFolk.employedAt, null);
        }
	}

	// Visits: Bakery, Grocery,Cheese Factory, Butchers
    public enum Stage
    {
        IDLE, ARRIVEDATSTORE, PICKUPBAKERY, PICKUPGROCERY, PICKUPCHEESE,PICKUPBUTCHERS, DROPOFF,HANGINGOUT
    }
    
    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if(theStore ==null) {
        	theStore=Building.getBuilding(theFolk.employedAt);
        }
        if (theStore==null) {
        	//wait until building has loaded
        	return;
        }

        if (!ModSimukraft.isDayTime())
        {
            theStage = Stage.IDLE;
        }

        super.onUpdateGoingToWork(theFolk);

        if (theStage == Stage.ARRIVEDATSTORE)
        {
            theFolk.action = FolkAction.ATWORK;
            runDelay = 11000;
        }
        else if (theStage==Stage.HANGINGOUT) {
        	runDelay=30000;
        }
        else
        {
            runDelay = 5000;
        }
        if (System.currentTimeMillis() - timeSinceLastRun < runDelay)
        {
            return;
        }
      

        // ////////////////IDLE
        if (theStage == Stage.IDLE && ModSimukraft.isDayTime())
        {
        }
        else if (theStage == Stage.ARRIVEDATSTORE)
        {
        	theFolk.statusText="Checking my errands list";
        	theStage=Stage.PICKUPBAKERY;
        	step=1;
        } else if (theStage==Stage.PICKUPBAKERY) {
        	stagePickupBakery();
        } else if (theStage==Stage.PICKUPGROCERY) {
        	stagePickupGrocery();
        } else if (theStage==Stage.PICKUPCHEESE) {
        	stagePickupCheese();
        } else if (theStage==Stage.PICKUPBUTCHERS) {
        	stagePickupButchers();
        } else if (theStage==Stage.DROPOFF) {
        	stageDropoff();
        } else if (theStage==Stage.HANGINGOUT) {
        	stageHangingOut();
        }

        if (!ModSimukraft.isDayTime())
        {
            theStage = Stage.IDLE;
        }

        timeSinceLastRun = System.currentTimeMillis();
    }
    
    /** generic do pick up for each type of building */
    private void doPickup(String buildingSearch,ItemStack pickUpItem, boolean doCompareMeta) {
    	if (step==1) {
    		currentPickup=0;
    		pickupBuildings.clear();
    		pickupBuildings=Building.getBuildingBySearch(buildingSearch, true);
    		if (pickupBuildings.size() ==0) { step=4; return; }
    		
    		theFolk.gotoXYZ(pickupBuildings.get(currentPickup).primaryXYZ, null);
    		theFolk.statusText="On my way to the "+pickupBuildings.get(currentPickup).displayName;
    		step=2;
    	
    	} else if (step==2) {
    		if (theFolk.destination ==null) {
    			step=3;
    		}
    	
    	} else if (step==3) {
    		theFolk.statusText="Buying items at the "+pickupBuildings.get(currentPickup).displayName;
    		ArrayList<IInventory> chests=inventoriesFindClosest(pickupBuildings.get(currentPickup).primaryXYZ, 5);
    		if (!chests.isEmpty()) {
	    		int count=getItemCountInChests(chests, pickUpItem, doCompareMeta);
	    		int buy=count/4;
	    		if (buy>0) {
	    			ModSimukraft.log.info("JobBurgersManager: buying "+buy+" out of "+count+" items");
	    			inventoriesTransferLimitedToFolk(theFolk.inventory, chests
	    					, new ItemStack(pickUpItem.itemID, 1, pickUpItem.getItemDamage()),buy,doCompareMeta);
	    			
	    		
	    		}
    		}
    		currentPickup++;
    		if (currentPickup <= (pickupBuildings.size()-1)) {
    			theFolk.gotoXYZ(pickupBuildings.get(currentPickup).primaryXYZ, null);
        		theFolk.statusText="On my way to the "+pickupBuildings.get(currentPickup).displayName;
        		step=2;
    		} else {
    			step=4;
    		}
    	}
    }
    

    private void stagePickupBakery() {
    	if (step<4) {
    		doPickup("bakery",new ItemStack(Item.bread),false);
    	} else {
    		theStage=Stage.PICKUPGROCERY;
    		step=1;
    	}
    }

    private void stagePickupGrocery() {
    	if (step<4) {
    		doPickup("grocery",new ItemStack(Item.potato),false);
    	} else {
    		theStage=Stage.PICKUPCHEESE;
    		step=1;
    	}
    }
    
    private void stagePickupCheese() {
    	if (step<4) {
    		doPickup("cheese factory",new ItemStack(ModSimukraft.itemFood,1,0),true);
    	} else {
    		theStage=Stage.PICKUPBUTCHERS;
    		step=1;
    	}
    }
    
    private void stagePickupButchers() {
    	if (step<4) {
    		doPickup("butchers",new ItemStack(Item.beefRaw),false);
    	} else {
    		theStage=Stage.DROPOFF;
    		step=1;
    	}
    }
    
    private void stageDropoff() {
    	if (step==1) {
    		theFolk.statusText="On my way back to the store";
    		ArrayList<V3> back=theStore.getSpecialBlocks(0);
    		if (!back.isEmpty()) {
    			theFolk.gotoXYZ(back.get(0), null);
    			step=2;
    		}
    	
    	} else if (step==2) {
    		if (theFolk.destination==null) {
    			step=3;
    		}
    	
    	} else if (step==3) {
    		theFolk.statusText="Unloading ingredients";
    		ArrayList<V3> back=theStore.getSpecialBlocks(0);
    		ArrayList<IInventory> backstoreChests=inventoriesFindClosest(back.get(0), 3);
    		boolean ok=inventoriesTransferFromFolk(theFolk.inventory, backstoreChests, null);
    		if (!ok) {
    			ModSimukraft.sendChat(theFolk.name+": The chest in the kitchen at the Fast food store is full!");
    		}
    		theStage=Stage.HANGINGOUT;
    		step=0;
    		ModSimukraft.states.credits -=2.45;
    	}
    }
    
    private void stageHangingOut() {
    	if (step %2==0) {
    		theFolk.gotoXYZ(theStore.primaryXYZ, GotoMethod.WALK);
    	} else {
    		theFolk.gotoXYZ(theStore.getSpecialBlocks(0).get(0), GotoMethod.WALK);
    	}
    	
    	String say="";
		switch(step) {
		case 0: say="Counting today's takings"; break;
		case 1: say="Cancelling staff leave"; break;
		case 2: say="Being very bossy"; break;
		case 3: say="Doing my taxes"; break;
		case 4: say="Disciplining staff"; break;
		case 5: say="Reducing staff wages"; break;
		case 6: say="Adjusting menu font"; break;
		}
		step++;
		if (step>6) {step=0;}
		theFolk.statusText=say;
    }
    
	@Override
	public void onArrivedAtWork() {
        int dist = 0;
        dist = theFolk.location.getDistanceTo(theFolk.employedAt);

        if (dist <= 1)
        {
            theFolk.action = FolkAction.ATWORK;
            theFolk.stayPut = true;
            theFolk.statusText = "Arrived at the store";
            theStage = Stage.ARRIVEDATSTORE;
            ArrayList<V3> back=theStore.getSpecialBlocks(0);
            if (!back.isEmpty()) {
            	theFolk.gotoXYZ(back.get(0), null);
            }
        }
        else
        {
            theFolk.gotoXYZ(theFolk.employedAt, null);
        }
	}

	@Override
	public void resetJob() {
		theStage = Stage.IDLE;

	}

}
