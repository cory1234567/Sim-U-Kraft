package info.satscape.simukraft.common;

import info.satscape.simukraft.common.CommonProxy.V3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.ModLoader;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

public class PacketHandler implements IPacketHandler
{
    public PacketHandler()
    {
    }

    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        if (packet.channel.equals("SUKMain"))
        {
            DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));

            try
            {
                String par1 = inputStream.readUTF();
                String cmd = inputStream.readUTF();
                String val = inputStream.readUTF();
                World world = null;
                Side side = cpw.mods.fml.common.FMLCommonHandler.instance().getEffectiveSide();
                String sside = "none";

                if (side == Side.SERVER)
                {
                    sside = "Server";
                    
                    // CLINET GUI > SERVER, a building is being built, so load it server side
                    if(cmd.contentEquals("loadbuilding")) {
                    	Building.loadAllBuildings();
                    
					// Client side Windmill is requesting Colour meta value
                    }else if (cmd.contentEquals("requestMeta")) {
                    	world=MinecraftServer.getServer().worldServerForDimension(Integer.parseInt(val));
                    	
                    	V3 v3=new V3(par1);
	                    TileEntityWindmill te=(TileEntityWindmill) world.getBlockTileEntity(v3.x.intValue(),v3.y.intValue(),v3.z.intValue());
	                    if (te !=null) {
	                    	te.sendMetaToClient();
	                    } else {
	                    	ModSimukraft.log.warning("TEwindmill was null at "+v3.toString());
	                    }
                    }
                    
                }
                else if (side == Side.CLIENT)
                {
                    sside = "Client";
                    world = ModLoader.getMinecraftInstance().theWorld;
                    
                    /// SERVER > CLIENT windmill announcing it's meta value for the colour of the sails.
                    if (cmd.contentEquals("announceMeta")) {
	                    V3 v3=new V3(par1);
	                    TileEntityWindmill te=(TileEntityWindmill) world.getBlockTileEntity(v3.x.intValue(),v3.y.intValue(),v3.z.intValue());
	                    if (te !=null) {
	                    	te.meta=Integer.parseInt(val);
	                    } else {
	                    	ModSimukraft.log.warning("***TEwindmill was null at "+v3.toString());
	                    }
                    
	                /// SERVER > CLIENT folks position update (every 10 or so seconds for each folk)
                    } else if (cmd.contentEquals("updateFolkPosition")) {
                    	FolkData folk=FolkData.getFolkByName(par1);
                    	V3 newpos=new V3(val);
                    	if (folk !=null && newpos !=null) {
                    		folk.serverToClientLocationUpdate(newpos);
                    	}
                    
                    /// SERVER > CLIENT - player has changed worlds, so reset and load in new SUK data	
                    } else if (cmd.contentEquals("gamereset")) {
                    	ModSimukraft.resetAndLoadNewWorld();  // still bugs with this
                    }
                    
                }

                ModSimukraft.log.info("PacketHandler: "+sside + "-side PACKET RECIEVED: " + par1 + " - " + cmd + " = " + val);
                
                
                
                
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return;
            }
        }
    }

    public static Packet250CustomPayload makePacket(String par1, String cmd, String val)
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
        DataOutputStream outputStream = new DataOutputStream(bos);

        try
        {
            outputStream.writeUTF(par1);
            outputStream.writeUTF(cmd);
            outputStream.writeUTF(val);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = "SUKMain";
        packet.data = bos.toByteArray();
        packet.length = bos.size();
        ModSimukraft.log.info("PacketHandler: Packet sent: "+par1+" "+cmd+" "+val);
        return packet;
    }
}
