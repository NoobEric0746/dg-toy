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
import org.nooberic.dg_toy.client.model.DgCollarCurioModel;
import org.nooberic.dg_toy.item.ModItems;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

@Mod.EventBusSubscriber(modid = Dg_toy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DgCollarCurioRenderer implements ICurioRenderer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Dg_toy.MODID, "textures/item/dg_collar_curio.png");
    private static final float OFFSET_X = 0.01F;
    private static final float OFFSET_Y = 0.06F;
    private static final float OFFSET_Z = -0.07F;
    private static final float SCALE = 0.45F;
    private final DgCollarCurioModel model;

    private DgCollarCurioRenderer() {
        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        this.model = new DgCollarCurioModel(models.bakeLayer(DgCollarCurioModel.LAYER_LOCATION));
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(DgCollarCurioRenderer::onRegisterLayers);
        modEventBus.addListener(DgCollarCurioRenderer::onRegisterRenderers);
    }

    private static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DgCollarCurioModel.LAYER_LOCATION, DgCollarCurioModel::createBodyLayer);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        CuriosRendererRegistry.register(ModItems.DG_COLLAR.get(), DgCollarCurioRenderer::new);
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
        ICurioRenderer.followBodyRotations(slotContext.entity(), this.model);
        poseStack.pushPose();
        this.model.body.translateAndRotate(poseStack);
        poseStack.translate(OFFSET_X, OFFSET_Y, OFFSET_Z);
        poseStack.scale(SCALE, SCALE, SCALE);
        this.model.renderToBuffer(
                poseStack,
                buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
                packedLight,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );
        poseStack.popPose();
    }
}