package sekelsta.ride_along.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

import sekelsta.ride_along.RideAlong;

public class RidingPacketHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static void register(final RegisterPayloadHandlerEvent event) {
        IPayloadRegistrar registrar = event.registrar(RideAlong.MODID)
                .versioned(PROTOCOL_VERSION);
        registrar.play(CMountEntityPacket.ID, CMountEntityPacket::decode, CMountEntityPacket::handle);
    }
}
