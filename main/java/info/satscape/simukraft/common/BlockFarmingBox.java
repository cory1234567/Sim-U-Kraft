package info.satscape.simukraft.common;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import info.satscape.simukraft.client.Gui.GuiFarming;
import info.satscape.simukraft.common.CommonProxy.V3;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockFarmingBox extends Block
{
    private IIcon icons[];

    public BlockFarmingBox(int par1)
    {
        super(Material.wood);
        this.setCreativeTab(CreativeTabs.tabMisc);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister)
    {
        icons = new IIcon[1];
        icons[0] = iconRegister.registerIcon("satscapesimukraft:blockFarming");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
        return icons[0];
    }

    @Override
    public void onBlockDestroyedByPlayer(World par1World, int par2,
                                         int par3, int par4, int par5)
    {
        FolkData theFolk = FolkData.getFolkByEmployedAt(new V3((double)par2, (double)par3, (double)par4
                           , par1World.provider.dimensionId));

        if (theFolk != null)
        {
            theFolk.selfFire();
        }

        FarmingBox m;
        m = FarmingBox.getFarmingBlockByBoxXYZ(new V3((double)par2, (double)par3, (double)par4, par1World.provider.dimensionId));
        ModSimukraft.theFarmingBoxes.remove(m);
        par1World.playSoundEffect(par2, par3, par4, "satscapesimukraft:powerdown", 1f, 1f);
        super.onBlockDestroyedByPlayer(par1World, par2, par3, par4, par5);
    }

    @Override
    public void onBlockAdded(World par1World, int par2, int par3, int par4)
    {
    	if (BlockMarker.markers.isEmpty()) {
        	ModSimukraft.sendChat("You need to place down 3 markers first to mark out the farming area");
        	return;
        }
    	if (BlockMarker.markers.size() != 3) {
        	ModSimukraft.sendChat("You need to place down 3 markers, not "+BlockMarker.markers.size());
        	return;
        }
    	FarmingBox m;
        ModSimukraft.theFarmingBoxes.add(m = new FarmingBox(new V3((double)par2, (double)par3, (double)par4, par1World.provider.dimensionId)));

        try
        {
            int first = BlockMarker.markers.size() - 3;
            m.marker1XYZ = BlockMarker.markers.get(first).toV3();
            m.marker2XYZ = BlockMarker.markers.get(first + 1).toV3();
            m.marker3XYZ = BlockMarker.markers.get(first + 2).toV3();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        super.onBlockAdded(par1World, par2, par3, par4);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean onBlockActivated(World world, int i, int j, int k,
                                    EntityPlayer entityplayer, int par6, float par7, float par8,
                                    float par9)
    {
        world.playSoundEffect(i, j, k, "satscapesimukraft:computer", 1f, 1f);

        try
        {
            FarmingBox farmingBlock = FarmingBox
                                      .getFarmingBlockByBoxXYZ(new V3((double) i, (double) j,
                                              (double) k, entityplayer.dimension));
            farmingBlock.location.theDimension = entityplayer.dimension;
            FolkData folk = FolkData.getFolkByEmployedAt(new V3((double) i,
                            (double) j, (double) k, entityplayer.dimension));
            Minecraft mc = ModLoader.getMinecraftInstance();
            mc.displayGuiScreen(new GuiFarming(farmingBlock, folk));
        }
        catch (Exception e)
        {
            ModSimukraft.sendChat("Sorry, there was a problem with this farming box, try replacing it.");
        }

        return true;
    }
}
