package sekelsta.ride_along;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.passive.horse.*;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;

public final class EntityUtil {
    private static final String WEIGHT = "WeightKg";
    private static final String CHILD_WEIGHT = "ChildWeightKg";
    private static final String BULK = "Bulk";
    private static final String CHILD_BULK = "ChildBulk";
    private static final String STRENGTH = "Strength";
    private static final String CHILD_STRENGTH = "ChildStrength";

    public static boolean isBaby(Entity entity) {
        return entity instanceof AgeableEntity && ((AgeableEntity)entity).isBaby();
    }

    private static CompoundNBT getTag(Entity entity) {
        return entity.getPersistentData().getCompound("RideAlong");
    }

    public static double getWeight(Entity entity) {
        if (isBaby(entity)
            && getTag(entity).contains(CHILD_WEIGHT)) {
                return getTag(entity).getDouble(CHILD_WEIGHT);
        }

        if (getTag(entity).contains(WEIGHT)) {
            double weight = getTag(entity).getDouble(WEIGHT);
            if (isBaby(entity)) {
                return weight / 8;
            }
            return weight;
        }
        double weight = entity.getBoundingBox().getXsize()
                        * entity.getBoundingBox().getYsize()
                        * entity.getBoundingBox().getZsize();
        // Convert to kilograms on the basis that the player 
        // (hitbox 0.6 * 0.6 * 1.8 = 0.648) weighs 140 pounds / 63.5 kg
        weight *= 63.5 / 0.648;
        if (entity instanceof BatEntity || entity instanceof ParrotEntity) {
            weight *= 0.2;
        }
        else if (entity instanceof BeeEntity
                || entity instanceof ChickenEntity
                || entity instanceof CatEntity
                || entity instanceof FoxEntity
                || entity instanceof OcelotEntity
                || entity instanceof RabbitEntity) {
            weight *= 0.5;
        }
        else if (entity instanceof MuleEntity 
                || entity instanceof TurtleEntity
                || entity instanceof HorseEntity
                || entity instanceof PigEntity
                || entity instanceof CowEntity) {
            weight *= 1.5;
        }
        return weight;

    }

    public static double getBulk(Entity entity) {
        double bulk = 1;
        if (getTag(entity).contains(BULK)) {
            bulk = getTag(entity).getDouble(BULK);
        }
        else if (entity instanceof AnimalEntity) {
            bulk *= 2;
        }

        if (isBaby(entity)) {
            if (getTag(entity).contains(CHILD_BULK)) {
                bulk *= getTag(entity).getDouble(CHILD_BULK);
            }
            else {
                bulk *= 2;
            }
        }

        return bulk * getWeight(entity);
    }

    public static double getCapacity(Entity entity) {
        double strength = 0.1;
        if (getTag(entity).contains(STRENGTH)) {
            strength = getTag(entity).getDouble(STRENGTH);
        }
        else if (entity instanceof AbstractHorseEntity) {
            strength *= 2;
        }
        else if (entity instanceof ItemEntity) {
            strength *= 20;
        }

        if (isBaby(entity)) {
            if (getTag(entity).contains(CHILD_STRENGTH)) {
                strength *= getTag(entity).getDouble(CHILD_STRENGTH);
            }
        }

        double capacity = strength * getWeight(entity);

        for (Entity rider : entity.getPassengers()) {
            capacity -= getBulk(rider);
        }

        return capacity;
    }
}

