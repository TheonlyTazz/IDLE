package dev.theonlytazz.idlecinematics.api;

import dev.theonlytazz.idlecinematics.core.NamespacedId;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.Optional;

/** Read-only candidate passed to add-on landmark filters during bounded scene analysis. */
public record LandmarkCandidate(ClientLevel level, BlockPos position, BlockState state,
                                Optional<BlockEntity> blockEntity, NamespacedId blockId) {
    public LandmarkCandidate {
        Objects.requireNonNull(level, "level");
        position = Objects.requireNonNull(position, "position").immutable();
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(blockEntity, "blockEntity");
        Objects.requireNonNull(blockId, "blockId");
    }
}
