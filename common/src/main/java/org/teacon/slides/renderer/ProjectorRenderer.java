package org.teacon.slides.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.teacon.slides.config.Config;
import org.teacon.slides.mappings.BlockEntityRendererMapper;
import org.teacon.slides.projector.ProjectorBlock;
import org.teacon.slides.projector.ProjectorBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.net.URI;

@ParametersAreNonnullByDefault
public class ProjectorRenderer extends BlockEntityRendererMapper<ProjectorBlockEntity> {
	public ProjectorRenderer(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public void render(ProjectorBlockEntity tile, float partialTick, PoseStack pStack,
					   MultiBufferSource source, int packedLight, int packedOverlay) {

		// render bounding box for DEBUG
//        renderBoundingBox(pStack, tile);

		// always update slide state
		final boolean projectorDisabled = tile.getBlockState().getValue(BlockStateProperties.POWERED);
		if(projectorDisabled) return;

		final Slide slide = SlideState.getSlide(tile.mLocation, !tile.mDisableLod);

		if (slide == null) {
			return;
		}

		// Render
        int color = tile.mColor;
        if ((color & 0xFF000000) == 0) {
            return;
        }

        pStack.pushPose();

        PoseStack.Pose last = pStack.last();
        Matrix4f pose = last.pose();
        Matrix3f normal = last.normal();

        BlockState state = tile.getBlockState();
        // get direction
        Direction direction = state.getValue(BlockStateProperties.FACING);
        // get internal rotation
        ProjectorBlock.InternalRotation rotation = state.getValue(ProjectorBlock.ROTATION);
        // matrix 1: translation to block center
        pStack.translate(0.5f, 0.5f, 0.5f);
        // matrix 2: rotation
        pose.rotate(direction.getRotation());
        normal.rotate(direction.getRotation());
		// matrix 2a: User rotation
		float rotX = (float)((Math.PI * tile.mRotateX) / 180);
		float rotY = (float)((Math.PI * tile.mRotateY) / 180);
		float rotZ = (float)((Math.PI * tile.mRotateZ) / 180);

		pStack.mulPose(new Quaternionf(rotX, rotY, rotZ, 1));
        // matrix 3: translation to block surface
        pStack.translate(0.0f, 0.5f, 0.0f);
        // matrix 4: internal rotation
        rotation.transform(pose);
        rotation.transform(normal);

        // matrix 5: translation for slide
        pStack.translate(-0.5F, 0.0F, 0.5F - tile.mHeight);

        // matrix 6: offset for slide
        pStack.translate(tile.mOffsetX, -tile.mOffsetZ, tile.mOffsetY);
        // matrix 7: scaling
        pose.scale(tile.mWidth, 1.0F, tile.mHeight);

        final boolean flipped = tile.getBlockState().getValue(ProjectorBlock.ROTATION).isFlipped();

        slide.render(source, last.pose(), last.normal(), tile.mWidth, tile.mHeight, color, packedLight,
                OverlayTexture.NO_OVERLAY, flipped || tile.mDoubleSided, !flipped || tile.mDoubleSided, !tile.mDisableLod,
                SlideState.getAnimationTick(), partialTick);

        pStack.popPose();

        boolean canShowDiscordHighlight = Minecraft.getInstance().player.isCreative() || Minecraft.getInstance().player.isSpectator();

		// Render Discord Highlight
		if(Config.getDiscordVisualizerEnabled() && canShowDiscordHighlight) {
			try {
				URI uri = new URI(tile.mLocation);
				if(uri.getHost().contains("discord")) {
					pStack.pushPose();
					VertexConsumer builder = source.getBuffer(RenderType.textSeeThrough(new ResourceLocation("slide_show:textures/gui/slide_default.png")));
					builder.vertex(pStack.last().pose(), 0, 0, 1)
							.color(220, 40, 40, 255).uv(0.5F, 0.6F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();
					builder.vertex(pStack.last().pose(), 1, 0, 1)
							.color(220, 40, 40, 255).uv(0.6F, 0.6F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();
					builder.vertex(pStack.last().pose(), 1, 1, 1)
							.color(220, 40, 40, 255).uv(0.6F, 0.4F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();
					builder.vertex(pStack.last().pose(), 0, 1, 1)
							.color(220, 40, 40, 255).uv(0.5F, 0.4F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();
					// Face 2
					builder.vertex(pStack.last().pose(), 1, 0, 0)
							.color(220, 40, 40, 255).uv(0.5F, 0.6F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();
					builder.vertex(pStack.last().pose(), 0, 0, 0)
							.color(220, 40, 40, 255).uv(0.6F, 0.6F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();
					builder.vertex(pStack.last().pose(), 0, 1, 0)
							.color(220, 40, 40, 255).uv(0.6F, 0.4F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();
					builder.vertex(pStack.last().pose(), 1, 1, 0)
							.color(220, 40, 40, 255).uv(0.5F, 0.4F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();

					// Face 3
					builder.vertex(pStack.last().pose(), 0, 0, 0)
							.color(220, 40, 40, 255).uv(0.5F, 0.6F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();
					builder.vertex(pStack.last().pose(), 0, 0, 1)
							.color(220, 40, 40, 255).uv(0.6F, 0.6F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();
					builder.vertex(pStack.last().pose(), 0, 1, 1)
							.color(220, 40, 40, 255).uv(0.6F, 0.4F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();
					builder.vertex(pStack.last().pose(), 0, 1, 0)
							.color(220, 40, 40, 255).uv(0.5F, 0.4F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();

					// Face 4

					builder.vertex(pStack.last().pose(), 1, 0, 1)
							.color(220, 40, 40, 255).uv(0.5F, 0.6F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();
					builder.vertex(pStack.last().pose(), 1, 0, 0)
							.color(220, 40, 40, 255).uv(0.6F, 0.6F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();
					builder.vertex(pStack.last().pose(), 1, 1, 0)
							.color(220, 40, 40, 255).uv(0.6F, 0.4F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();
					builder.vertex(pStack.last().pose(), 1, 1, 1)
							.color(220, 40, 40, 255).uv(0.5F, 0.4F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();

					// top
					builder.vertex(pStack.last().pose(), 0, 1, 1)
							.color(255, 40, 40, 255).uv(0.5F, 0.6F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();
					builder.vertex(pStack.last().pose(), 1, 1, 1)
							.color(255, 40, 40, 255).uv(0.6F, 0.6F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();
					builder.vertex(pStack.last().pose(), 1, 1, 0)
							.color(255, 40, 40, 255).uv(0.6F, 0.4F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();
					builder.vertex(pStack.last().pose(), 0, 1, 0)
							.color(255, 40, 40, 255).uv(0.5F, 0.4F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();

					// bottom
					// top
					builder.vertex(pStack.last().pose(), 1, 0, 1)
							.color(255, 40, 40, 255).uv(0.5F, 0.6F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();
					builder.vertex(pStack.last().pose(), 0, 0, 1)
							.color(255, 40, 40, 255).uv(0.6F, 0.6F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();
					builder.vertex(pStack.last().pose(), 0, 0, 0)
							.color(255, 40, 40, 255).uv(0.6F, 0.4F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();
					builder.vertex(pStack.last().pose(), 1, 0, 0)
							.color(255, 40, 40, 255).uv(0.5F, 0.4F)
							.uv2(OverlayTexture.NO_OVERLAY)
							.normal(pStack.last().normal(), 0, 1, 0).endVertex();
					pStack.popPose();
				}
			} catch (Exception ignored) {

			}
		}
	}

	@Override
	public boolean shouldRenderOffScreen(ProjectorBlockEntity tile) {
		// global rendering
		return true;
	}

	@Environment(EnvType.CLIENT)
	public int getViewDistance() {
		return Config.getViewDistance();
	}
}
