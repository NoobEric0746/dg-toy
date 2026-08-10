package org.nooberic.dg_toy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.client.model.DgCollarCurioModel;
import org.nooberic.dg_toy.item.ModItems;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

@EventBusSubscriber(modid = Dg_toy.MODID, value = Dist.CLIENT)
public final class DgCollarCurioRenderer implements ICurioRenderer {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Dg_toy.MODID, "textures/item/dg_collar_curio.png");
    private static final float OFFSET_X = 0.01F;
    private static final float OFFSET_Y = 0.06F;
    private static final float OFFSET_Z = -0.07F;
    private static final float SCALE = 0.45F;
    private final DgCollarCurioModel model;

    private DgCollarCurioRenderer() {
        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        this.model = new DgCollarCurioModel(models.bakeLayer(DgCollarCurioModel.LAYER_LOCATION));
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DgCollarCurioModel.LAYER_LOCATION, DgCollarCurioModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        CuriosRendererRegistry.register(ModItems.DG_COLLAR.get(), DgCollarCurioRenderer::new);
    }

    @Override
    public <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int packedLight,
            S renderState,
            RenderLayerParent<S, M> renderLayerParent,
            EntityRendererProvider.Context context,
            float yRotation,
            float xRotation
    ) {
        ICurioRenderer.setupHumanoidAnimations(this.model, renderState);
        poseStack.pushPose();
        this.model.body.translateAndRotate(poseStack);
        poseStack.translate(OFFSET_X, OFFSET_Y, OFFSET_Z);
        poseStack.scale(SCALE, SCALE, SCALE);
        submitModel(this.model, renderState, poseStack, submitNodeCollector, packedLight);
        poseStack.popPose();
    }

    private static void submitModel(Model<?> model, LivingEntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight) {
        @SuppressWarnings("unchecked")
        Model<LivingEntityRenderState> renderModel = (Model<LivingEntityRenderState>) (Model<?>) model;
        submitNodeCollector.order(1).submitModel(
                renderModel,
                renderState,
                poseStack,
                RenderTypes.armorCutoutNoCull(TEXTURE),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                -1,
                null,
                renderState.outlineColor,
                null
        );
    }
}
