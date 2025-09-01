/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.entity.LivingEntityMoveEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.Vec3d;

public class GhastSpeed extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<GhastSpeed.VerticalMode> verticalMode = sgGeneral.add(new EnumSetting.Builder<VerticalMode>()
        .name("vertical-mode")
        .description("The way to move up and down")
        .defaultValue(VerticalMode.BoatFly)
        .build()
    );

    private final Setting<Double> horizontalSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("horizontal-speed")
        .description("Horizontal speed in blocks per second for happy ghast.")
        .defaultValue(10)
        .min(0)
        .sliderMax(50)
        .visible(() -> verticalMode.get() != VerticalMode.BoatFly)
        .build()
    );

    private final Setting<Double> verticalSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("vertical-speed")
        .description("Vertical speed in blocks per second for happy ghast.")
        .defaultValue(5)
        .min(0)
        .sliderMax(50)
        .visible(() -> verticalMode.get() != VerticalMode.BoatFly)
        .build()
    );

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("Speed in blocks per second for happy ghast.")
        .defaultValue(10)
        .min(0)
        .sliderMax(50)
        .visible(() -> verticalMode.get() != VerticalMode.Pitch)
        .build()
    );

    private final Setting<Boolean> inWater = sgGeneral.add(new BoolSetting.Builder()
        .name("in-water")
        .description("Use speed when in water.")
        .defaultValue(false)
        .build()
    );

    public GhastSpeed() {
        super(Categories.Movement, "ghast-speed", "Makes you go faster and control better when riding happy ghasts.");
    }

    @EventHandler
    private void onLivingEntityMove(LivingEntityMoveEvent event) {
        if (event.entity.getControllingPassenger() != mc.player) return;

        // Check for inWater
        LivingEntity entity = event.entity;
        if (!inWater.get() && entity.isTouchingWater()) return;

        // Set horizontal velocity
        Vec3d vel = PlayerUtils.getHorizontalVelocity(switch (verticalMode.get()) {
            case Pitch -> speed.get();
            case BoatFly -> horizontalSpeed.get();
        });

        // Set vertical velocity
        double velY = PlayerUtils.getVerticalVelocity(switch (verticalMode.get()) {
            case Pitch -> speed.get();
            case BoatFly -> verticalSpeed.get();
        }, switch (verticalMode.get()) {        // Pass down verticalMode
            case Pitch -> true;
            case BoatFly -> false;
        });

        // Check if on happy_ghast
        if (Registries.ENTITY_TYPE.getId(entity.getType()).getPath().equals("happy_ghast")) {
            ((IVec3d) event.movement).meteor$setXZ(vel.x, vel.z);
            ((IVec3d) event.movement).meteor$setY(velY);
        }
    }

    public enum VerticalMode {
        BoatFly,
        Pitch
    }
}
