package sekelsta.ride_along.network;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import sekelsta.ride_along.RideAlong;
import sekelsta.ride_along.EntityUtil;

public record CMountEntityPacket(int entityId) implements CustomPacketPayload {
    public static final Type<CMountEntityPacket> TYPE = new Type<>(new ResourceLocation(RideAlong.MODID, "cmount"));

    public static final StreamCodec<FriendlyByteBuf, CMountEntityPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, CMountEntityPacket::entityId, CMountEntityPacket::new);

    public CMountEntityPacket(int entityId) {
        this.entityId = entityId;
    }

    public CMountEntityPacket(Entity entity) {
        this(entity.getId());
    }

    @Override
    public Type<CMountEntityPacket> type() {
        return TYPE;
    }

    private boolean tryMounting(Entity rider, Entity mount) {
        if (EntityUtil.getBulk(rider) > EntityUtil.getCapacity(mount)) {
            return false;
        }

        return rider.startRiding(mount);
    }

    public void handle(IPayloadContext context) {
        Player sender = context.player();
        Entity target = sender.level().getEntity(this.entityId);
        if (target == null) {
            RideAlong.logger.warn("Could not find entity with id " + this.entityId + " requested by " 
                + sender.getName().getString());
            return;
        }
        if (!EntityUtil.isValidTarget(target)) {
            return;
        }
        if (EntityUtil.isValidRider(target)) {
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
        }
        // Else if the target is not being ridden by a player, dismount all
        // its passengers
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
    }
}
