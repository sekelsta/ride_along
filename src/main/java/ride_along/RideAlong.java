package sekelsta.ride_along;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.ArrayUtils;

import sekelsta.ride_along.network.RidingPacketHandler;
import sekelsta.ride_along.ClientEventHandler;

@Mod(RideAlong.MODID)
public class RideAlong {
    public static final String MODID = "ride_along";

    public static RideAlong instance;

    public static Logger logger = LogManager.getLogger(MODID);

    public RideAlong(IEventBus modEventBus)
    {
        instance = this;

        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(RidingPacketHandler::register);
    }

    private void clientSetup(final FMLClientSetupEvent event)
    {
        NeoForge.EVENT_BUS.addListener(ClientEventHandler::handleInteract);
        Minecraft.getInstance().options.keyMappings = ArrayUtils.add(Minecraft.getInstance().options.keyMappings, ClientEventHandler.keyRide);
    }
}
