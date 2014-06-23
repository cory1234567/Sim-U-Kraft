package info.satscape.simukraft.common;

import java.util.List;
import java.util.Random;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockLightBox extends Block
{
    private IIcon icons[];

    public BlockLightBox(int blockId)
    {
        super(Material.wood);
        setBlockName("Light Box");
        setLightLevel(1.0F);
        this.setCreativeTab(CreativeTabs.tabMisc);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister)
    {
        icons = new IIcon[8];
        icons[0] = iconRegister.registerIcon("satscapesimukraft:blockLightWhite");
        icons[1] = iconRegister.registerIcon("satscapesimukraft:blockLightRed");
        icons[2] = iconRegister.registerIcon("satscapesimukraft:blockLightOrange");
        icons[3] = iconRegister.registerIcon("satscapesimukraft:blockLightYellow");
        icons[4] = iconRegister.registerIcon("satscapesimukraft:blockLightGreen");
        icons[5] = iconRegister.registerIcon("satscapesimukraft:blockLightBlue");
        icons[6] = iconRegister.registerIcon("satscapesimukraft:blockLightPurple");
        icons[7] = iconRegister.registerIcon("satscapesimukraft:blockLightRainbow");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
    	return meta < 8 && meta >= 0 ? icons[meta] : icons[0];
    }

    @Override
    public int idDropped(int par1, Random par2Random, int par3)
    {
        return this.blockID;
    }

    @Override
    public int damageDropped(int j)
    {
        return j;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(int blockId, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (int meta = 0; meta < 8; ++meta)
        {
            par3List.add(new ItemStack(blockId, 1, meta));
        }
    }

    @Override
    public int getBlockColor()
    {
        return 0xffffff;
    }
}
