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

public class ItemWindmillBase extends Item {

	private IIcon icons[];
	
	public ItemWindmillBase(int par1) {
		super();
		setUnlocalizedName("Windmill Base");
		this.setHasSubtypes(true);
		this.setCreativeTab(CreativeTabs.tabBlock);
	    maxStackSize = 64;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		icons= new IIcon[1];
		icons[0] = iconRegister.registerIcon("satscapesimukraft:windmillbase");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int par1) {
		return icons[0];
	}

	/*@Override
	public String getItemDisplayName(ItemStack par1ItemStack) {
		return "Windmill base";
	}*/

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack par1ItemStack,
			EntityPlayer par2EntityPlayer, List par3List, boolean par4) {

		par3List.add("Craft sails to make a windmill");
		super.addInformation(par1ItemStack, par2EntityPlayer,par3List , par4);
	}
	
	@Override
	public IIcon getIcon(ItemStack stack, int pass) {
		return icons[0];
	}

}
