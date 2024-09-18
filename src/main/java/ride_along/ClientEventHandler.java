package sekelsta.ride_along;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.neoforged.neoforge.network.PacketDistributor;

import sekelsta.ride_along.network.CMountEntityPacket;
import sekelsta.ride_along.network.RidingPacketHandler;

public class ClientEventHandler {
    @OnlyIn(Dist.CLIENT)
    public static final KeyMapping keyRide = new KeyMapping(RideAlong.MODID + ".key.ride", 341, "key.categories.misc");

    @OnlyIn(Dist.CLIENT)
    public static void handleInteract(EntityInteract event) {
        if (keyRide.isDown()) {
            CMountEntityPacket packet = new CMountEntityPacket(event.getTarget());
            PacketDistributor.SERVER.noArg().send(packet);
            // TODO: Set interaction result to SUCCESS
        }
    }
}
