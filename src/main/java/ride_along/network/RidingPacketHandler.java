package sekelsta.ride_along.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import sekelsta.ride_along.RideAlong;

public class RidingPacketHandler {
    private static int ID = 0;

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        // Name
        new ResourceLocation(RideAlong.MODID, "main"),
        // Protocol version supplier
        () -> PROTOCOL_VERSION,
        // Predicate - client compatible protocol versions
        PROTOCOL_VERSION::equals,
        // Predicate - server compatible protocol versions
        PROTOCOL_VERSION::equals
    );

    public static void registerPackets() {
        CHANNEL.registerMessage(ID++, CMountEntityPacket.class, 
            CMountEntityPacket::encode, CMountEntityPacket::decode, 
            CMountEntityPacket::handle);
    }
}
