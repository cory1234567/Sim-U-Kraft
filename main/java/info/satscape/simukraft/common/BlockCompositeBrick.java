package info.satscape.simukraft.common;

import java.util.List;

import info.satscape.simukraft.client.Gui.GuiBuildingConstructor;
import info.satscape.simukraft.common.CommonProxy.V3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockCompositeBrick extends Block {
    @SideOnly(Side.CLIENT)
    private IIcon[] icons;
    
	public BlockCompositeBrick(int par1, Material par2Material) {
		super(Material.rock);
		setBlockName("Composite Brick");
		this.setCreativeTab(CreativeTabs.tabBlock);
	}

	  @Override
	    @SideOnly(Side.CLIENT)
	    public void registerBlockIcons(IIconRegister iconRegister)
	    {
	        icons = new IIcon[1];
	        icons[0] = iconRegister.registerIcon("satscapesimukraft:compositebrick");
	    }

	    @Override
	    @SideOnly(Side.CLIENT)
	    public IIcon getIcon(int side, int meta)      // getBlockTextureFromSideAndMetadata
	    {
	        return icons[0];
	    }


}
