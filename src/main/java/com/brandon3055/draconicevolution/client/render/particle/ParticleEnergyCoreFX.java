package com.brandon3055.draconicevolution.client.render.particle;

import com.brandon3055.brandonscore.client.ClientProxy;
import com.brandon3055.brandonscore.client.particle.IntParticleType;
import com.brandon3055.brandonscore.lib.Vec3D;
import com.brandon3055.brandonscore.utils.BCProfiler;
import com.brandon3055.brandonscore.utils.Utils;
import com.brandon3055.draconicevolution.client.handler.ClientEventHandler;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Direction;

/**
 * Created by brandon3055 on 2/5/2016.
 * The particle used to render the beams on the Energy Core
 */
public class ParticleEnergyCoreFX extends SpriteTexturedParticle {

    public static final IParticleRenderType PARTICLE_NO_DEPTH_NO_LIGHT = new IParticleRenderType() {
        public void begin(BufferBuilder builder, TextureManager manager) {
            RenderSystem.depthMask(false);
            manager.bind(AtlasTexture.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.alphaFunc(516, 0.003921569F);
            builder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        }

        public void end(Tessellator tesselator) {
            tesselator.end();
        }

        public String toString() {
            return "PARTICLE_NO_DEPTH_NO_LIGHT";
        }
    };

    public Vec3D targetPos;
    public boolean toCore = false;
    public int startRotation = 0;
    private Direction.Axis direction;
    public boolean isLargeStabilizer = false;
    private final IAnimatedSprite spriteSet;

    public ParticleEnergyCoreFX(ClientWorld world, double xPos, double yPos, double zPos, Vec3D targetPos, IAnimatedSprite spriteSet) {
        super(world, xPos, yPos, zPos);
        this.targetPos = targetPos;
        this.spriteSet = spriteSet;
        setSprite(spriteSet.get(world.random));
        hasPhysics = false;
        Vec3D dir = Vec3D.getDirectionVec(new Vec3D(xPos, yPos, zPos), targetPos);
        this.direction = Direction.getNearest((float) dir.x, (float) dir.y, (float) dir.z).getAxis();
        lifetime = 20;
    }

    @Override
    public IParticleRenderType getRenderType() {
        return PARTICLE_NO_DEPTH_NO_LIGHT;
    }

    @Override
    public void tick() {
        BCProfiler.TICK.start("core_fx_update");
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        Vec3D tPos = this.targetPos.copy();
        setSprite(spriteSet.get(level.random));

        if (toCore) {
            double rotation = ClientEventHandler.elapsedTicks;
            double offsetX = Math.sin((rotation / 180D * Math.PI) + (startRotation / 100D));
            double offsetY = Math.cos((rotation / 180D * Math.PI) + (startRotation / 100D));

            double d = isLargeStabilizer ? 1.8 : 0.2;
            if (direction == Direction.Axis.Z) {
                tPos.add(offsetX * d, offsetY * d, 0);
            }
            else if (direction == Direction.Axis.Y) {
                tPos.add(offsetX * d, 0, offsetY * d);
            }
            else if (direction == Direction.Axis.X) {
                tPos.add(0, offsetY * d, offsetX * d);
            }
        }

        Vec3D dir = Vec3D.getDirectionVec(new Vec3D(x, y, z), tPos);
        double speed = (toCore ? 0.5D : 0.25D);
        xd = dir.x * speed;
        yd = dir.y * speed;
        zd = dir.z * speed;
        move(xd, yd, zd);

        if (age++ > lifetime || Utils.getDistanceAtoB(x, y, z, tPos.x, tPos.y, tPos.z) < 0.2) {
            remove();
        }
        BCProfiler.TICK.stop();
    }

    public static class Factory implements IParticleFactory<IntParticleType.IntParticleData> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite p_i50823_1_) {
            this.spriteSet = p_i50823_1_;
        }

        @Override
        public Particle createParticle(IntParticleType.IntParticleData data, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ParticleEnergyCoreFX particle = new ParticleEnergyCoreFX(world, x, y, z, new Vec3D(xSpeed, ySpeed, zSpeed), spriteSet);
            particle.toCore = data.get().length >= 1 && data.get()[0] == 1;
            particle.startRotation = data.get().length >= 2 ? data.get()[1] : 0;
            particle.isLargeStabilizer = data.get().length >= 3 && data.get()[2] == 1;
            particle.scale(particle.isLargeStabilizer ? 2 : 1);
            return particle;
        }
    }
}
