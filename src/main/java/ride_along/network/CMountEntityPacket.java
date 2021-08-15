package sekelsta.ride_along.network;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.passive.WaterMobEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import sekelsta.ride_along.RideAlong;
import sekelsta.ride_along.EntityUtil;

public class CMountEntityPacket {
    private int entityId;

    public CMountEntityPacket() {}

    public CMountEntityPacket(int entityId) {
        this.entityId = entityId;
    }

    public CMountEntityPacket(Entity entity) {
        this(entity.getId());
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeVarInt(this.entityId);
    }

    public static CMountEntityPacket decode(PacketBuffer buffer) {
        int id = buffer.readVarInt();
        return new CMountEntityPacket(id);
    }

    private boolean tryMounting(Entity rider, Entity mount) {
        // Ban boats and whatnot from riding
        if (!(rider instanceof LivingEntity)) {
            return false;
        }
        // No aquatic riders
        if (rider instanceof WaterMobEntity) {
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
    private void handleMain(Context context) {
        ServerPlayerEntity sender = context.getSender();
        Entity target = sender.level.getEntity(this.entityId);
        System.out.println("Type:" + target.getType() + "\nIs baby: " + EntityUtil.isBaby(target) + "\nWeight (Kg): " + EntityUtil.getWeight(target));
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
        if (target instanceof PlayerEntity) {
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
        List<MobEntity> entities = sender.level.getEntitiesOfClass(
            MobEntity.class, 
            sender.getBoundingBox().inflate(9, 4, 9),
            (entity) -> {
                return entity != target 
                    && entity.getLeashHolder() == sender;
            }
        );
        for (MobEntity entity : entities) {
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
                if (passenger instanceof PlayerEntity && passenger != sender) {
                    return;
                }
            }
            // Only if the target had a passenger before the click, though
            if (target.getPassengers().size() == 1 
                    && target.getControllingPassenger() == sender) {
                return;
            }
            target.ejectPassengers();
        }
    }

    public void handle(Supplier<Context> context) {
        // Enqueue anything that needs to be thread-safe
        context.get().enqueueWork(() -> {
            handleMain(context.get());
        });
        context.get().setPacketHandled(true);
    }
}
