package info.satscape.simukraft.common;

import java.util.ArrayList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import info.satscape.simukraft.client.Gui.GuiMining;
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

public class BlockMiningBox extends Block
{
    private IIcon icons[];

    public BlockMiningBox(int par1)
    {
        super(Material.wood);
        setBlockName("Mining Box");
        this.setCreativeTab(CreativeTabs.tabMisc);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister)
    {
        icons = new IIcon[1];
        icons[0] = iconRegister.registerIcon("satscapesimukraft:blockMining");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
        return icons[0];
    }

    @Override
    public void onBlockAdded(World world, int i, int j, int k)
    {
        if (BlockMarker.markers.isEmpty()) {
        	ModSimukraft.sendChat("You need to place down 3 markers first to mark out the mining area");
        	return;
        }
    	MiningBox m;
        ModSimukraft.theMiningBoxes.add(m = new MiningBox(new V3((double)i, (double)j, (double)k, world.provider.dimensionId)));

        if (BlockMarker.markers.size() == 1)
        {
            m.marker1XYZ = BlockMarker.markers.get(0).toV3();
            m.marker2XYZ = null;
            m.marker3XYZ = null;
        }
        else
        {
            try
            {
                int first = BlockMarker.markers.size() - 3;
                m.marker1XYZ = BlockMarker.markers.get(first).toV3();
                m.marker2XYZ = BlockMarker.markers.get(first + 1).toV3();
                m.marker3XYZ = BlockMarker.markers.get(first + 2).toV3();
            }
            catch (Exception e) {}
        }

        super.onBlockAdded(world, i, j, k);
    }

    @Override
    public void onBlockDestroyedByPlayer(World world, int i, int j, int k, int meta)
    {
        FolkData theFolk = FolkData.getFolkByEmployedAt(new V3((double)i, (double)j, (double)k
                           , world.provider.dimensionId));

        if (theFolk != null)
        {
            theFolk.selfFire();
        }

        MiningBox m;
        m = MiningBox.getMiningBlockByBoxXYZ(new V3(i, j, k));
        ModSimukraft.theMiningBoxes.remove(m);
        world.playSoundEffect(i, j, k, "satscapesimukraft:powerdown", 1f, 1f);
        super.onBlockDestroyedByPlayer(world, i, j, k, meta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean onBlockActivated(World world, int i, int j, int k,
                                    EntityPlayer entityplayer, int par6, float par7, float par8, float par9)
    {
        world.playSoundEffect(i, j, k, "satscapesimukraft:computer", 1f, 1f);
        MiningBox miningBlock  = MiningBox.getMiningBlockByBoxXYZ(new V3((double)i, (double)j, (double)k, entityplayer.dimension));

        try
        {
            miningBlock.location.theDimension = entityplayer.dimension;
            ArrayList<FolkData> folks = FolkData.getFolksByEmployedAt(new V3((double)i, (double)j, (double)k, entityplayer.dimension));
            GuiMining ui = new GuiMining(miningBlock, folks);
            Minecraft mc = ModLoader.getMinecraftInstance();
            mc.displayGuiScreen(ui);
        }
        catch (Exception e)
        {
            e.printStackTrace();

            if (world.isRemote)
            {
                ModSimukraft.sendChat("Sorry, there was a problem with this mining box, try place it again");
            }
        }

        return true;
    }
}
