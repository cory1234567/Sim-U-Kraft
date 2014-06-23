package info.satscape.simukraft.common;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;

//NOTE: Never used this
public class ItemGranulesCopper extends Item {
	private IIcon icons[];
	
	public ItemGranulesCopper(int par1) {
		super();
		setUnlocalizedName("Copper Granules");
		this.setHasSubtypes(true);
		this.setCreativeTab(CreativeTabs.tabMaterials);
		//maxStackSize = 64;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		icons= new IIcon[1];
		icons[0] = iconRegister.registerIcon("satscapesimukraft:granulesCopper");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int par1) {
		return icons[par1];
	}
	public static final String[] names = new String[] { "first", "second" };
	 
    @Override
    public String getUnlocalizedName(ItemStack par1ItemStack)
    {
        int i = MathHelper.clamp_int(par1ItemStack.getItemDamage(), 0, 15);
        return super.getUnlocalizedName() + "." + names[i];
    }

	@Override
	public IIcon getIcon(ItemStack stack, int pass) {
		return icons[0];
	}
	
	
}
