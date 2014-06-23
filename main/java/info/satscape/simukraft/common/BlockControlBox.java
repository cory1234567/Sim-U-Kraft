package info.satscape.simukraft.common;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import info.satscape.simukraft.client.Gui.GuiBankATM;
import info.satscape.simukraft.client.Gui.GuiControlBox;
import info.satscape.simukraft.common.CommonProxy.V3;
import info.satscape.simukraft.common.ModSimukraft.GameMode;
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

public class BlockControlBox extends Block
{
    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    public BlockControlBox()
    {
        super(Material.wood);
        setBlockName("Control Box");
        this.setCreativeTab(CreativeTabs.tabMisc);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister)
    {
        icons = new IIcon[4];
        icons[0] = iconRegister.registerIcon("satscapesimukraft:blockControlTop");
        icons[1] = iconRegister.registerIcon("satscapesimukraft:blockControlSide");
        icons[2] = iconRegister.registerIcon("satscapesimukraft:blockATM");
        icons[3] = iconRegister.registerIcon("satscapesimukraft:blockControlTopOther");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
        if (meta == 0)  						//normal control box
        {
            if (side == 0 || side == 1) //top and bottom
            {
                return icons[0];
            }
            else                                //all sides
            {
                return icons[1];
            }
        }
        else if (meta == 1)  								//sim-u-bank control box
        {
            return icons[2];
        }
        else  							// meta=2... Other building control box (slightly different colour)
        {
            if (side == 0 || side == 1) //top and bottom
            {
                return icons[3];
            }
            else                                //all sides
            {
                return icons[1];
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer,
                                    int par6, float par7, float par8, float par9)
    {
        world.playSoundEffect(i, j, k, "satscapesimukraft:computer", 1f, 1f);
        GuiControlBox ui = null;
        GuiBankATM ui2 = null;
        Minecraft mc = ModLoader.getMinecraftInstance();
        mc.setIngameNotInFocus();

        if (world.getBlockMetadata(i, j, k) == 0 || world.getBlockMetadata(i, j, k) == 2)
        {
            ui = new GuiControlBox(new V3((double)i, (double)j, (double)k, entityplayer.dimension), entityplayer);
            mc.displayGuiScreen(ui);
        }
        else
        {
            if (ModSimukraft.gameMode == GameMode.CREATIVE)
            {
                mc.displayGuiScreen(null);
                ModSimukraft.sendChat("The Bank is not active when in Creative Mode (as there's no money!)");
            }
            else
            {
                ui2 = new GuiBankATM(new V3((double)i, (double)j, (double)k, entityplayer.dimension), entityplayer);
                mc.displayGuiScreen(ui2);
            }
        }

        return true;
    }

    public int quantityDropped(Random random)
    {
        return 0;   // no recipe and no drop
    }
}
