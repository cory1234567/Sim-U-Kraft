package info.satscape.simukraft.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;

import cpw.mods.fml.relauncher.Side;

// TODO: A lot of this is no longer used and should be removed

public class GameStates implements Serializable
{
    private static final long serialVersionUID = -2617900998876928361L;

    public int population = 0;              // TBR
    public float credits = 10.00f;
    public boolean cheatMode = false;       //TBR  

   // public int runMod = -1;  
    public int gameModeNumber = -1;  // -1 not asked   0=normal  1=creative 2=hardcore 10=DO NOT RUN MOD
    public int dayOfWeek = 0; //0 to 6 use getDayOfWeek() to return string
    public long lastUpdateCheck = 0l; //last time we checked for new buildings and update version
    public long userId = 0l;

    //NOT USED - Now in config file, but kept here for compatibility - ALL TBR
    public int populationLimit = 100;     ///
    public int lumberArea = 30;			 ///
    public boolean disableBeamEffect = false; ///
    public boolean folkNoise = true;        ///

    
    public GameStates()
    {
    }

    public void loadStates() {
    	
    		File f=new File(ModSimukraft.getSavesDataFolder()+"settings.sk2");
    		if (!f.exists()) {
    			ModSimukraft.states = (GameStates) ModSimukraft.proxy.
    					loadObject(ModSimukraft.getSavesDataFolder() + "settings.suk");
    		} else {
    			loadStates2();
    		}	
    	
    }
    
    //new sk2 loader
    private void loadStates2() {
    	ArrayList<String> strings=ModSimukraft.loadSK2(ModSimukraft.getSavesDataFolder()+"settings.sk2");
    	for(String line:strings) {
    		if (line.contains("|")) {
    			int m1=line.indexOf("|");
				String name=line.substring(0,m1);
				String value=line.substring(m1+1);
				
    			try {
	    			if (name.contentEquals("credits")) {
	    				credits=Float.parseFloat(value);
	    			}else if (name.contentEquals("gamemode")) {
	    				gameModeNumber=Integer.parseInt(value);
	    			}else if (name.contentEquals("dayofweek")) {
	    				dayOfWeek=Integer.parseInt(value);
	    			}else if (name.contentEquals("lastupdatecheck")) {
	    				lastUpdateCheck=Long.parseLong(value);
	    			}else if (name.contentEquals("uid")) {
	    				userId=Long.parseLong(value);
	    			}
    			} catch(Exception e) {e.printStackTrace(); }
    		}
    	}
    	//ModSimukraft.log("**** loadStates2 called - credits now = "+credits);
    }
    
    public void saveStates()
    {
        String folder = ModSimukraft.getSavesDataFolder();
        //ModSimukraft.proxy.saveObject(folder + "settings.suk", this);
       
   //     Side side = cpw.mods.fml.common.FMLCommonHandler.instance().getEffectiveSide();
   // 	if (side==Side.SERVER) { 
	        ArrayList<String> strings=new ArrayList<String>();
	        strings.add("credits|"+credits);
	        strings.add("gamemode|"+gameModeNumber);
	        strings.add("dayofweek|"+dayOfWeek);
	        strings.add("lastupdatecheck|"+lastUpdateCheck);
	       // strings.add("uid|"+userId);
	        ModSimukraft.saveSK2(folder+"settings.sk2", strings);
	        ModSimukraft.log.info("GameStates: saveStates() called BOTH sides, credits saved as "+credits);
    //	} else {
    		//ModSimukraft.log("**** saveStates() on CLIENT SIDE, no save done");
    //	}
        
    }
}
