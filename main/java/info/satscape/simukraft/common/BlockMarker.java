package info.satscape.simukraft.common;

import info.satscape.simukraft.client.Gui.GuiMarker;

import java.util.ArrayList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.src.ModLoader;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import info.satscape.simukraft.common.CommonProxy.V3;

public class BlockMarker extends Block
{
    public static boolean hasPlaced = false;
    public static ArrayList<Marker> markers = new ArrayList<Marker>();
    public V3 location;

    private IIcon icons[];

    public BlockMarker()
    {
        super(Material.wood);
        setBlockName("Marker");
        this.setCreativeTab(CreativeTabs.tabMisc);
        setBlockBounds(0.4f, 0.0f, 0.4f,  0.6f, 0.9f, 0.6f);
        this.setLightLevel(0.1f);
    }

    @SideOnly(Side.CLIENT)
    
    @Override
    public void registerBlockIcons(IIconRegister par1IconRegister)
    {
        icons = new IIcon[1];
        icons[0] = par1IconRegister.registerIcon("satscapesimukraft:blockMarker");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int par1, int par2)      // getBlockTextureFromSideAndMetadata
    {
        switch(par2)
        {
        case 0:
        	return icons[0];
        case 1:
        {
        	switch (par1)
        	{
        	case 0:
        		return icons[1];
        	case 1:
        		return icons[2];
        	default:
        		return icons[3];
        	}
        }
        default:
        {
        	System.out.println("Invalid metadata for " + this.getUnlocalizedName());
        	return icons[0];
        }
        }
    }

    @Override
    public void setBlockBoundsForItemRender()
    {
        setBlockBounds(0.4f, 0.0f, 0.4f,  0.6f, 0.9f, 0.6f); //post shaped
    }

    public boolean renderAsNormalBlock()
    {
        return false;
    }

    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public void onBlockDestroyedByPlayer(World world, int i, int j, int k, int meta)
    {
        try
        {
            for (int m = 0; m < markers.size(); m++)
            {
                Marker marker = markers.get(m);

                for (int mm = 0; mm < 4; mm++)
                {
                    try
                    {
                        marker.beams.get(mm).setDead();
                    }
                    catch (Exception e) {}
                }
            }
        }
        catch (Exception e) {}  //index out of bounds when destroying too many

        markers.clear();
        super.onBlockDestroyedByPlayer(world, i, j, k, meta);
    }

    @Override
    public void onBlockAdded(World world, int i, int j, int k)
    {
        hasPlaced = true;
        Marker ma;
        markers.add(ma = new Marker(i, j, k, world.provider.dimensionId));
        String markerCaption = "";
        String helpText = "";

        if (markers.size() == 1)
        {
            markerCaption = "Front-Left";
            helpText = "You can place two more markers to mark out an area for a farm or mine etc. If you wish to do this, place another marker at the front-right position now";
        }
        else if (markers.size() == 2)
        {
            markerCaption = "Front-Right";
            helpText = "Finally, place a marker at the Rear-Left position";
        }
        else if (markers.size() == 3)
        {
            markerCaption = "Rear-Left";
            helpText = "You're done, now you can place down a mining box, farming box or right-click the front-left marker to copy a structure!";
        }
        else
        {
            markerCaption = "Too many Markers!";
        }

        if (markers.size() < 4)
        {
            V3 pos = new V3((double)i, (double)j, (double)k, world.provider.dimensionId);
            pos.y += 0.01d;

            if (ModSimukraft.configEnableMarkerAlignmentBeams)
            {
                EntityAlignBeam beam = new EntityAlignBeam(world);
                ma.caption = markerCaption;
                beam.setLocationAndAngles(pos.x, pos.y, pos.z, 0f, 0f);
                beam.yaw = 0f;

                if (!world.isRemote)
                {
                    world.spawnEntityInWorld(beam);
                }

                ma.beams.add(beam);
                EntityAlignBeam beam2 = new EntityAlignBeam(world);
                beam2.setLocationAndAngles(pos.x, pos.y, pos.z, 90f, 0f);
                beam2.yaw = 90f;

                if (!world.isRemote)
                {
                    world.spawnEntityInWorld(beam2);
                }

                ma.beams.add(beam2);
                EntityAlignBeam beam3 = new EntityAlignBeam(world);
                beam3.setLocationAndAngles(pos.x, pos.y, pos.z, 180f, 0f);
                beam3.yaw = 180f;

                if (!world.isRemote)
                {
                    world.spawnEntityInWorld(beam3);
                }

                ma.beams.add(beam3);
                EntityAlignBeam beam4 = new EntityAlignBeam(world);
                beam4.setLocationAndAngles(pos.x, pos.y, pos.z, 270f, 0f);
                beam4.yaw = 270f;

                if (!world.isRemote)
                {
                    world.spawnEntityInWorld(beam4);
                }

                ma.beams.add(beam4);
            }
        }

        if (!helpText.contentEquals(""))
        {
            ModSimukraft.sendChat(helpText);
        }

        super.onBlockAdded(world, i, j, k);
    }

    public static Marker getMarker(V3 position)
    {
        Marker ret = null;

        for (int i = 0; i < markers.size(); i++)
        {
            Marker m = markers.get(i);

            if (m.x == position.x && m.y == position.y && m.z == position.z)
            {
                ret = m;
                break;
            }
        }

        return ret;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean onBlockActivated(World world, int i, int j, int k,
                                    EntityPlayer entityplayer, int par6, float par7, float par8, float par9)
    {
        this.location = new V3((double)i, (double)j, (double)k, entityplayer.dimension);
        world.playSoundEffect(i, j, k, "satscapesimukraft:computer", 1f, 1f);
        GuiMarker ui = new GuiMarker(this.location, entityplayer);
        Minecraft mc = ModLoader.getMinecraftInstance();
        mc.displayGuiScreen(ui);
        return true;
    }
}
