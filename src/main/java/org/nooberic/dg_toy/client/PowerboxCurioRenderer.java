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
import org.nooberic.dg_toy.client.model.PowerboxCurioModel;
import org.nooberic.dg_toy.item.ModItems;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

@EventBusSubscriber(modid = Dg_toy.MODID, value = Dist.CLIENT)
public final class PowerboxCurioRenderer implements ICurioRenderer {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Dg_toy.MODID, "textures/item/powerbox_curio.png");
    // PoseStack translations in the renderer are in block units (16 model units = 1 block).
    // The powerbox geometry ("bone") is centered at model-root (0.5, 16, -1), i.e. 0.25 blocks
    // below the left hip pivot. OFFSET_* / SCALE are fine-tuning knobs for thigh placement.
    // Rendered band center (blocks below the neck): (OFFSET_X + 0.03125*(1+SCALE), OFFSET_Y + 1.0 + SCALE, OFFSET_Z - 0.0625*(1+SCALE)).
    // -Y = up, +X = toward the player's left leg, -Z = toward the body front.
    private static final float OFFSET_X = 0.08F;
    private static final float OFFSET_Y = -0.32F;
    private static final float OFFSET_Z = -0.06F;
    private static final float SCALE = 0.32F;
    private final PowerboxCurioModel model;

    private PowerboxCurioRenderer() {
        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        this.model = new PowerboxCurioModel(models.bakeLayer(PowerboxCurioModel.LAYER_LOCATION));
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PowerboxCurioModel.LAYER_LOCATION, PowerboxCurioModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        CuriosRendererRegistry.register(ModItems.POWERBOX.get(), PowerboxCurioRenderer::new);
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
        this.model.leftLeg.translateAndRotate(poseStack);
        // The scale shrinks geometry toward the origin, so the translate places the origin such
        // that the scaled band lands at the thigh. The band itself sits 1 block below the model root.
        poseStack.translate(OFFSET_X - 0.0875F, OFFSET_Y + 0.25F, OFFSET_Z - 0.0625F);
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
