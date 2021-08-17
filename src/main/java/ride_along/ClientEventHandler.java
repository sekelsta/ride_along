package sekelsta.ride_along;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;

import sekelsta.ride_along.network.CMountEntityPacket;
import sekelsta.ride_along.network.RidingPacketHandler;

public class ClientEventHandler {
    @OnlyIn(Dist.CLIENT)
    public static void handleInteract(EntityInteract event) {
        if (RideAlong.keyRide.isDown()) {
            CMountEntityPacket packet = new CMountEntityPacket(event.getTarget());
            RidingPacketHandler.CHANNEL.sendToServer(packet);
        }
    }
}
