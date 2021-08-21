package sekelsta.ride_along;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sekelsta.ride_along.network.RidingPacketHandler;
import sekelsta.ride_along.ClientEventHandler;

@Mod(RideAlong.MODID)
public class RideAlong {
    public static final String MODID = "ride_along";

    public static RideAlong instance;

    public static Logger logger = LogManager.getLogger(MODID);

    public static final KeyMapping keyRide = new KeyMapping(MODID + ".key.ride", 341, "key.categories.misc");

    public RideAlong()
    {
        instance = this;

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        RidingPacketHandler.registerPackets();
    }

    private void clientSetup(final FMLClientSetupEvent event)
    {
        MinecraftForge.EVENT_BUS.addListener(ClientEventHandler::handleInteract);
        ClientRegistry.registerKeyBinding(keyRide);
    }
}
