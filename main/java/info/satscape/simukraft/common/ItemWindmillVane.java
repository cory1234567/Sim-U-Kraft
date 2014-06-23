package info.satscape.simukraft.common;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemWindmillVane extends Item {

	private IIcon icons[];
	
	public ItemWindmillVane(int par1) {
		super();
		setUnlocalizedName("Copper Granules");
		this.setHasSubtypes(true);
		this.setCreativeTab(CreativeTabs.tabMaterials);
	    //maxStackSize = 64;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		icons=new IIcon[16];
		for(int i=0;i<=15;i++) {
			icons[i] = iconRegister.registerIcon("satscapesimukraft:windmillvane"+i);
		}
	}
	
	@Override
	public IIcon getIconFromDamage(int meta)
	{
		if (meta >=0 && meta < 16) {
			return icons[meta];
		} else {
			return icons[0];
		}
	}

	@SideOnly(Side.CLIENT)
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
	    for (int x = 0; x < 16; x++)
	    {
	        par3List.add(new ItemStack(this, 1, x));
	    }
	}
	
	@Override
    public String getUnlocalizedName(ItemStack is)
    {
        return "item.windmillvane"+is.getItemDamage();
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack par1ItemStack,
			EntityPlayer par2EntityPlayer, List par3List, boolean par4) {

		par3List.add("Craft 4 of these to make a sail");
		super.addInformation(par1ItemStack, par2EntityPlayer,par3List , par4);
	}

	@Override
    public int getMetadata(int par1)
    {
        return par1;
    }

}
