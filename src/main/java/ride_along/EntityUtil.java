package sekelsta.ride_along;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.horse.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;

public final class EntityUtil {
    private static final String WEIGHT = "WeightKg";
    private static final String CHILD_WEIGHT = "ChildWeightKg";
    private static final String BULK = "Bulk";
    private static final String CHILD_BULK = "ChildBulk";
    private static final String STRENGTH = "Strength";
    private static final String CHILD_STRENGTH = "ChildStrength";

    public static boolean isBaby(Entity entity) {
        return entity instanceof AgeableMob && ((AgeableMob)entity).isBaby();
    }

    private static CompoundTag getTag(Entity entity) {
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
        if (entity instanceof Bat || entity instanceof Parrot) {
            weight *= 0.2;
        }
        else if (entity instanceof Bee
                || entity instanceof Chicken
                || entity instanceof Cat
                || entity instanceof Fox
                || entity instanceof Ocelot
                || entity instanceof Rabbit) {
            weight *= 0.5;
        }
        else if (entity instanceof Mule 
                || entity instanceof Turtle
                || entity instanceof Horse
                || entity instanceof Pig
                || entity instanceof Cow) {
            weight *= 1.5;
        }
        else if (!(entity instanceof LivingEntity)) {
            weight *= 2;
        }
        return weight;

    }

    public static double getBulk(Entity entity) {
        double bulk = 1;
        if (getTag(entity).contains(BULK)) {
            bulk = getTag(entity).getDouble(BULK);
        }
        else if (entity instanceof Animal) {
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
        double strength = 0.15;
        if (getTag(entity).contains(STRENGTH)) {
            strength = getTag(entity).getDouble(STRENGTH);
        }
        else if (entity instanceof AbstractHorse) {
            strength = 0.22;
        }
        else if (!(entity instanceof LivingEntity)) {
            strength = 100;
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

