package com.sing.astatine.mixin.misc;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

// Thanks Lithium mod by JellySquid!
// This file is open source under GNU LESSER GENERAL PUBLIC LICENSE,for full version please see a copy in the project root named "LICENSE-LGPL"!

@Mixin(BlockPos.class)
public abstract class MixinBlockPos extends Vec3i {

    public MixinBlockPos(int x, int y, int z) {
        super(x, y, z);
    }

    /**
     * @author JellySquid
     * @reason Simplify and inline
     */
    // Name changed,above->up
    @Overwrite
    public BlockPos up() {
        return new BlockPos(this.getX(), this.getY() + 1, this.getZ());
    }

    /**
     * @author JellySquid
     * @reason Simplify and inline
     */
    @Overwrite
    // Name changed,above->up
    public BlockPos up(int distance) {
        return new BlockPos(this.getX(), this.getY() + distance, this.getZ());
    }

    /**
     * @author JellySquid
     * @reason Simplify and inline
     */
    @Overwrite
    // Name changed,below->down
    public BlockPos down() {
        return new BlockPos(this.getX(), this.getY() - 1, this.getZ());
    }

    /**
     * @author JellySquid
     * @reason Simplify and inline
     */
    @Overwrite
    // Name changed,below->down
    public BlockPos down(int distance) {
        return new BlockPos(this.getX(), this.getY() - distance, this.getZ());
    }

    /**
     * @author JellySquid
     * @reason Simplify and inline
     */
    @Overwrite
    public BlockPos north() {
        return new BlockPos(this.getX(), this.getY(), this.getZ() - 1);
    }

    /**
     * @author JellySquid
     * @reason Simplify and inline
     */
    @Overwrite
    public BlockPos north(int distance) {
        return new BlockPos(this.getX(), this.getY(), this.getZ() - distance);
    }

    /**
     * @author JellySquid
     * @reason Simplify and inline
     */
    @Overwrite
    public BlockPos south() {
        return new BlockPos(this.getX(), this.getY(), this.getZ() + 1);
    }

    /**
     * @author JellySquid
     * @reason Simplify and inline
     */
    @Overwrite
    public BlockPos south(int distance) {
        return new BlockPos(this.getX(), this.getY(), this.getZ() + distance);
    }

    /**
     * @author JellySquid
     * @reason Simplify and inline
     */
    @Overwrite
    public BlockPos west() {
        return new BlockPos(this.getX() - 1, this.getY(), this.getZ());
    }

    /**
     * @author JellySquid
     * @reason Simplify and inline
     */
    @Overwrite
    public BlockPos west(int distance) {
        return new BlockPos(this.getX() - distance, this.getY(), this.getZ());
    }

    /**
     * @author JellySquid
     * @reason Simplify and inline
     */
    @Overwrite
    public BlockPos east() {
        return new BlockPos(this.getX() + 1, this.getY(), this.getZ());
    }

    /**
     * @author JellySquid
     * @reason Simplify and inline
     */
    @Overwrite
    public BlockPos east(int distance) {
        return new BlockPos(this.getX() + distance, this.getY(), this.getZ());
    }
}
