package info.satscape.simukraft.common;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;
import net.minecraft.block.Block;

public class PricesForBlocks implements Serializable
{
    private static final long serialVersionUID = -2617939458756927761L;

    /** base price is how much per block the merchant will pay YOU for it
     *   when he sells you blocks the price will be base + 12% (his/her mark-up)
     */
    private static Float basePricePlanks = 0.0131f;
    private static Float basePriceLogs = 0.0131f * 4;
    private static Float basePriceCobblestone = 0.0032f;
    private static Float basePriceStone = 0.0141f;
    private static Float basePriceGlass = 0.0121f;
    private static Float basePriceWool = 0.0115f;
    private static Float basePriceBrick = 0.0251f;
    private static Float basePriceStonebrick = 0.0261f;
    private static Float basePriceFence = 0.0113f;

    public static Float bankPriceDiamond = 10.23f;
    public static Float bankPriceEmerald = 9.34f;
    public static Float bankPriceRedstone = 3.75f;
    public static Float bankPriceGlowstone = 2.48f;
    public static Float bankPriceGold = 5.12f;

    /** get the buy for price or the sell for price of the specified pack of 64 blocks */
    public static Float getPrice(Block block, boolean isBuying)
    {
        float base = 0f;

        if (block == Block.planks)
        {
            base = basePricePlanks;
        }
        else if (block == Block.wood)
        {
            base = basePriceLogs;
        }
        else if (block == Block.cobblestone)
        {
            base = basePriceCobblestone;
        }
        else if (block == Block.stone)
        {
            base = basePriceStone;
        }
        else if (block == Block.glass)
        {
            base = basePriceGlass;
        }
        else if (block == Block.cloth)
        {
            base = basePriceWool;
        }
        else if (block == Block.brick)
        {
            base = basePriceBrick;
        }
        else if (block == Block.stoneBrick)
        {
            base = basePriceStonebrick;
        }
        else if (block == Block.fence)
        {
            base = basePriceFence;
        }

        base = (base * 64); //per pack price

        if (isBuying)
        {
            base += (base * 1.12); //merchant's 12% markup
        }

        return base;
    }

    public static Float getPrice(int blockId, boolean isBuying)
    {
        float base = 0f;

        if (blockId == Block.planks.blockID)
        {
            base = basePricePlanks;
        }
        else if (blockId == Block.wood.blockID)
        {
            base = basePriceLogs;
        }
        else if (blockId == Block.cobblestone.blockID)
        {
            base = basePriceCobblestone;
        }
        else if (blockId == Block.stone.blockID)
        {
            base = basePriceStone;
        }
        else if (blockId == Block.glass.blockID)
        {
            base = basePriceGlass;
        }
        else if (blockId == Block.cloth.blockID)
        {
            base = basePriceWool;
        }
        else if (blockId == Block.brick.blockID)
        {
            base = basePriceBrick;
        }
        else if (blockId == Block.stoneBrick.blockID)
        {
            base = basePriceStonebrick;
        }
        else if (blockId == Block.fence.blockID)
        {
            base = basePriceFence;
        }

        base = (base * 64); //per pack price

        if (isBuying)
        {
            base += (base * 1.12); //merchant's 12% markup
        }

        return base;
    }

    /** set price PER BLOCK (not 64 blocks) */
    public static void setPrice(Block block, float newPrice)
    {
        if (block == Block.planks)
        {
            basePricePlanks = newPrice;
        }
        else if (block == Block.wood)
        {
            basePriceLogs = newPrice;
        }
        else if (block == Block.cobblestone)
        {
            basePriceCobblestone = newPrice;
        }
        else if (block == Block.stone)
        {
            basePriceStone = newPrice;
        }
        else if (block == Block.glass)
        {
            basePriceGlass = newPrice;
        }
        else if (block == Block.cloth)
        {
            basePriceWool = newPrice;
        }
        else if (block == Block.brick)
        {
            basePriceBrick = newPrice;
        }
        else if (block == Block.stoneBrick)
        {
            basePriceStonebrick = newPrice;
        }
        else if (block == Block.fence)
        {
            basePriceFence = newPrice;
        }
    }

    /** adjust the price up or down for sell and buy price for the block passed in,
     *  afterBuying is an adjustment after buying this block
     *  this is also called once a day for each block to fluctuate the prices */
    public static void adjustPrice(Block block, boolean afterBuying)
    {
        /// after selling the price goes down, so each sucessive sell gets less money
        /// after buying the price goes up
        /// make sure logs are 4 x planks
        /// min= 0.012     max=0.990
        Random r = new Random();
        float cprice;

        if (afterBuying)
        {
            cprice = PricesForBlocks.getPrice(block, false) / 64;
            cprice += (r.nextFloat() / 100);

            if (cprice > 0.99)
            {
                cprice = 0.99f;
            }

            PricesForBlocks.setPrice(block, cprice);
        }
        else
        {
            cprice = PricesForBlocks.getPrice(block, false) / 64;
            cprice -= (r.nextFloat() / 100);

            if (cprice < 0.012)
            {
                cprice = 0.012f;
            }

            PricesForBlocks.setPrice(block, cprice);
        }

        if (block == Block.planks)
        {
            PricesForBlocks.setPrice(Block.wood, cprice * 4);
        }

        if (block == Block.wood)  // Logs
        {
            PricesForBlocks.setPrice(Block.planks, cprice / 4);
        }
    }

    public static String formatPrice(float price)
    {
        NumberFormat formatter = new DecimalFormat("#0.00");
        return formatter.format(price);
    }
}
