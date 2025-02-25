package sekelsta.ride_along.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import sekelsta.ride_along.RideAlong;

public class RidingPacketHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static void register(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(RideAlong.MODID)
                .versioned(PROTOCOL_VERSION);
        registrar.playBidirectional(CMountEntityPacket.TYPE, CMountEntityPacket.STREAM_CODEC, CMountEntityPacket::handle);
    }
}
