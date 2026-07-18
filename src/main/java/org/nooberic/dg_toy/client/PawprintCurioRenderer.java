package org.nooberic.dg_toy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.client.model.PawprintCurioModel;
import org.nooberic.dg_toy.item.ModItems;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

@Mod.EventBusSubscriber(modid = Dg_toy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PawprintCurioRenderer implements ICurioRenderer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Dg_toy.MODID, "textures/item/pawprint_curio.png");
    private static final float OFFSET_X = 0.13F;
    private static final float OFFSET_Y = -0.06F;
    private static final float OFFSET_Z = 0.0F;
    private static final float SCALE = 1F-0.64F;
    private final PawprintCurioModel model;

    private PawprintCurioRenderer() {
        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        this.model = new PawprintCurioModel(models.bakeLayer(PawprintCurioModel.LAYER_LOCATION));
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(PawprintCurioRenderer::onRegisterLayers);
        modEventBus.addListener(PawprintCurioRenderer::onRegisterRenderers);
    }

    private static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PawprintCurioModel.LAYER_LOCATION, PawprintCurioModel::createBodyLayer);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        CuriosRendererRegistry.register(ModItems.PAWPRINT.get(), PawprintCurioRenderer::new);
    }

    @Override
    public <T extends net.minecraft.world.entity.LivingEntity, M extends EntityModel<T>> void render(
            net.minecraft.world.item.ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            net.minecraft.client.renderer.entity.RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource buffer,
            int packedLight,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        boolean rightArm = slotContext.index() == 1;
        ICurioRenderer.followBodyRotations(slotContext.entity(), this.model);
        poseStack.pushPose();

        if (rightArm) {
            this.model.rightArm.translateAndRotate(poseStack);
            poseStack.translate(-OFFSET_X, OFFSET_Y, OFFSET_Z);
            poseStack.scale(-SCALE, SCALE, SCALE);
            poseStack.last().normal().scale(-1.0F, 1.0F, 1.0F);
        } else {
            this.model.leftArm.translateAndRotate(poseStack);
            poseStack.translate(OFFSET_X, OFFSET_Y, OFFSET_Z);
            poseStack.scale(SCALE, SCALE, SCALE);
        }

        this.model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), packedLight, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}