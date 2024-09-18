package sekelsta.ride_along.network;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import sekelsta.ride_along.RideAlong;
import sekelsta.ride_along.EntityUtil;

public class CMountEntityPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(RideAlong.MODID, "cmount");

    private int entityId;

    public CMountEntityPacket() {}

    public CMountEntityPacket(int entityId) {
        this.entityId = entityId;
    }

    public CMountEntityPacket(Entity entity) {
        this(entity.getId());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.entityId);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static CMountEntityPacket decode(FriendlyByteBuf buffer) {
        int id = buffer.readVarInt();
        return new CMountEntityPacket(id);
    }

    private boolean tryMounting(Entity rider, Entity mount) {
        // Ban boats and whatnot from riding
        if (!(rider instanceof LivingEntity)) {
            return false;
        }
        // No aquatic riders
        if (rider instanceof WaterAnimal) {
            return false;
        }

        if (EntityUtil.getBulk(rider) > EntityUtil.getCapacity(mount)) {
            return false;
        }

        return rider.startRiding(mount);
    }

    // Helper for handle which runs on the main thread
    // This is a separate method from handle so that return statements can
    // be used for control flow, while still marking the packet handled
    // afterwards.
    private void handleMain(PlayPayloadContext context) {
        Player sender = context.player().get();
        Entity target = sender.level().getEntity(this.entityId);
        if (target == null) {
            RideAlong.logger.warn("Could not find entity with id " + this.entityId + " requested by " 
                + sender.getName().getString());
            return;
        }
        // Cannot use this to move hostile mobs
        if (!target.getType().getCategory().isFriendly()) {
            return;
        }
        // Cannot control other players
        if (target instanceof Player) {
            return;
        }
        // If mounted and the creature fits, mount it behind you
        if (sender.isPassenger()) {
            Entity mount = sender.getVehicle();
            if (tryMounting(target, mount)) {
                return;
            }
        }
        // Otherwise try to mount it on an entity leashed to you
        List<Mob> entities = sender.level().getEntitiesOfClass(
            Mob.class, 
            sender.getBoundingBox().inflate(9, 4, 9),
            (entity) -> {
                return entity != target 
                    && entity.getLeashHolder() == sender;
            }
        );
        for (Mob entity : entities) {
            if (tryMounting(target, entity)) {
                return;
            }
        }
        // Else if the target is not being ridden by a player, dismount all
        // its passengers
        // Check friendly to avoid silly stuff like dismounting skeleton horse riders
        // (Still allows dismounting chicken jockeys but whatever)
        if (target.getType().getCategory().isFriendly()) {
            for (Entity passenger : target.getPassengers()) {
                if (passenger instanceof Player && passenger != sender) {
                    return;
                }
            }
            // Only if the target had a passenger before the click, though
            if (target.getPassengers().size() == 1 
                    && target.getControllingPassenger() == sender) {
                return;
            }
            target.ejectPassengers();
            return;
        }
        // If we haven't used the click by now, try as if this mod didn't exist
        // TODO
    }

    public void handle(PlayPayloadContext context) {
        // Handle on main thread
        context.workHandler().execute(() -> {
            handleMain(context);
        });
    }
}
