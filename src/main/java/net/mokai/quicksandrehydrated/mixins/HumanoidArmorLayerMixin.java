package net.mokai.quicksandrehydrated.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.mokai.quicksandrehydrated.client.render.coverage.ArmorCoverageTextureCache;
import net.mokai.quicksandrehydrated.client.render.coverage.CoverageTextureUtils;
import net.mokai.quicksandrehydrated.entity.coverage.PlayerCoverage;
import net.mokai.quicksandrehydrated.entity.playerStruggling;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {

    @Inject(method = "renderArmorPiece", at = @At("TAIL"))
    private void qsrehydrated$renderCoverageArmor(PoseStack poseStack,
                                                  MultiBufferSource buffer,
                                                  T entity,
                                                  EquipmentSlot slot,
                                                  int packedLight,
                                                  A model,
                                                  CallbackInfo ci) {
        if (!(entity instanceof playerStruggling struggling)) {
            return;
        }

        PlayerCoverage coverage = struggling.getCoverage();
        if (coverage == null || coverage.coverageEntries.isEmpty()) {
            return;
        }

        ItemStack stack = entity.getItemBySlot(slot);
        if (!(stack.getItem() instanceof ArmorItem armorItem)) {
            return;
        }

        TextureAtlasSprite[] coverageByPixel = CoverageTextureUtils.buildCoverageByPixel(coverage);
        TextureAtlasSprite depthMask = CoverageTextureUtils.getArmorDepthMask(slot);

        ResourceLocation armorTexture = ((HumanoidArmorLayer<?, ?, ?>) (Object) this)
            .getArmorResource(entity, stack, slot, null);
        ResourceLocation coverageTexture = ArmorCoverageTextureCache.getCoverageTexture(
            entity,
            armorTexture,
            coverageByPixel,
            depthMask,
            slot == EquipmentSlot.LEGS,
            coverage.renderGeneration
        );

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentCull(coverageTexture));
        model.renderToBuffer(
            poseStack,
            consumer,
            packedLight,
            LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
            1.0F,
            1.0F,
            1.0F,
            1.0F
        );
    }
}

