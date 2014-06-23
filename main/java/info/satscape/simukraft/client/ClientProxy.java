package info.satscape.simukraft.client;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import info.satscape.simukraft.common.CommonProxy;
import info.satscape.simukraft.common.CommonTickHandler;
import info.satscape.simukraft.common.EntityAlignBeam;
import info.satscape.simukraft.common.EntityConBox;
import info.satscape.simukraft.common.EntityFolk;
import info.satscape.simukraft.common.EntityWindmill;
import info.satscape.simukraft.common.ModSimukraft;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;

public class ClientProxy extends CommonProxy
{
    @Override
    public void registerRenderInfo()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityAlignBeam.class, new RenderAlignBeam(new ModelAlignBeam()));
        RenderingRegistry.registerEntityRenderingHandler(EntityFolk.class, new RenderFolk(new ModelFolkFemale()));
        RenderingRegistry.registerEntityRenderingHandler(EntityConBox.class, new RenderConBox(new ModelConBox()));
        RenderingRegistry.registerEntityRenderingHandler(EntityWindmill.class, new RenderWindmill(new ModelWindmill()));
    }

    @Override
    public void registerMisc()
    {
        super.registerMisc();
        TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
    }

    @Override
    public World getClientWorld()
    {
        return FMLClientHandler.instance().getClient().theWorld;
    }


    @Override
    public void saveObject(String filename, Object o)
    {
        if (o == null)
        {

            return;
        }

        //System.out.println("Saving object "+o.toString());
        try
        {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(o);
            oos.flush();
            oos.close();
        }
        catch (Exception e)
        {
 
            e.printStackTrace();
        }
    }
}
