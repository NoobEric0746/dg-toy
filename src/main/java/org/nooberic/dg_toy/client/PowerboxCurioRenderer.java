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
import org.nooberic.dg_toy.client.model.PowerboxCurioModel;
import org.nooberic.dg_toy.item.ModItems;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curios.api.SlotContext;

@Mod.EventBusSubscriber(modid = Dg_toy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PowerboxCurioRenderer implements ICurioRenderer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Dg_toy.MODID, "textures/item/powerbox_curio.png");
    private static final float OFFSET_X = -0.15F;
    private static final float OFFSET_Y = 0.55F;
    private static final float OFFSET_Z = 0.0F;
    private static final float SCALE = 0.45F;
    private final PowerboxCurioModel model;

    private PowerboxCurioRenderer() {
        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        this.model = new PowerboxCurioModel(models.bakeLayer(PowerboxCurioModel.LAYER_LOCATION));
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(PowerboxCurioRenderer::onRegisterLayers);
        modEventBus.addListener(PowerboxCurioRenderer::onRegisterRenderers);
    }

    private static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PowerboxCurioModel.LAYER_LOCATION, PowerboxCurioModel::createBodyLayer);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        CuriosRendererRegistry.register(ModItems.POWERBOX.get(), PowerboxCurioRenderer::new);
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
        ICurioRenderer.translateIfSneaking(poseStack, slotContext.entity());
        this.model.leftLeg.translateAndRotate(poseStack);
        poseStack.translate(OFFSET_X, OFFSET_Y, OFFSET_Z);
        poseStack.scale(SCALE, SCALE, SCALE);
        this.model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), packedLight, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}