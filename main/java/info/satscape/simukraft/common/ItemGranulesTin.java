package info.satscape.simukraft.common;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

//NOTE: Like the copper one, Never used this
public class ItemGranulesTin extends Item {
	private IIcon icons[];
	
	public ItemGranulesTin(int par1) {
		super();
		setUnlocalizedName("Tin Granules");
		this.setHasSubtypes(true);
		this.setCreativeTab(CreativeTabs.tabMaterials);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		icons=new IIcon[1];
		icons[0] = iconRegister.registerIcon("satscapesimukraft:granulesTin");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int par1) {
		return icons[0];
	}

	@Override
	public IIcon getIcon(ItemStack stack, int pass) {
		return icons[0];
	}
}
