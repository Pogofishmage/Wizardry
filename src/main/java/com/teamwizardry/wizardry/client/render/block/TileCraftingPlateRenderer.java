package com.teamwizardry.wizardry.client.render.block;

import com.teamwizardry.librarianlib.core.client.ClientTickHandler;
import com.teamwizardry.librarianlib.features.math.interpolate.position.InterpBezier3D;
import com.teamwizardry.wizardry.api.block.CachedStructure;
import com.teamwizardry.wizardry.api.block.IStructure;
import com.teamwizardry.wizardry.api.capability.CapManager;
import com.teamwizardry.wizardry.api.render.ClusterObject;
import com.teamwizardry.wizardry.api.util.RandUtil;
import com.teamwizardry.wizardry.client.fx.LibParticles;
import com.teamwizardry.wizardry.common.tile.TileCraftingPlate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

/**
 * Created by Saad on 6/11/2016.
 */
public class TileCraftingPlateRenderer extends TileEntitySpecialRenderer<TileCraftingPlate> {

	@Override
	public void render(TileCraftingPlate te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (te.getBlockType() instanceof IStructure) {
			int maxTime = 30000;
			long diff = System.currentTimeMillis() - te.structureReveal;
			if (diff <= maxTime) {
				float prog = 1 - ((float) diff / (float) maxTime);

				IStructure structure = ((IStructure) te.getBlockType());

				GlStateManager.pushMatrix();
				GlStateManager.enableAlpha();
				GlStateManager.enableLighting();
				GlStateManager.enableBlend();
				GlStateManager.enableCull();
				GlStateManager.disableDepth();

				GlStateManager.translate(x, y, z);
				GlStateManager.translate(-structure.offsetToCenter().getX(), -structure.offsetToCenter().getY(), -structure.offsetToCenter().getZ());
				Minecraft mc = Minecraft.getMinecraft();
				Tessellator tes = Tessellator.getInstance();
				BufferBuilder buffer = tes.getBuffer();

				CachedStructure cachedStructure = ((IStructure) te.getBlockType()).getStructure();

				mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

				for (BlockRenderLayer layer : cachedStructure.blocks.keySet()) {
					buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
					buffer.addVertexData(cachedStructure.vboCaches.get(layer));

					for (int i = 0; i < buffer.getVertexCount(); i++) {
						int idx = buffer.getColorIndex(i + 1);
						buffer.putColorRGBA(idx, 255, 255, 255, (int) (255 * prog));
					}
					tes.draw();
				}

				GlStateManager.disableAlpha();
				GlStateManager.disableBlend();
				GlStateManager.disableCull();
				GlStateManager.enableDepth();
				GlStateManager.popMatrix();
				return;
			}
		}

		CapManager manager = new CapManager(te.cap);

		int count = te.inventory.size();
		for (ClusterObject cluster : te.inventory) {
			double timeDifference = (te.getWorld().getTotalWorldTime() - cluster.worldTime + partialTicks) / cluster.destTime;
			Vec3d current = cluster.origin.add(cluster.dest.subtract(cluster.origin).scale(MathHelper.sin((float) (timeDifference * Math.PI / 2))));

			if (!manager.isManaEmpty()) {
				if (!te.isCrafting && RandUtil.nextInt(count > 0 && count / 2 > 0 ? count / 2 : 1) == 0)
					LibParticles.CLUSTER_DRAPE(te.getWorld(), new Vec3d(te.getPos()).addVector(0.5, 0.5, 0.5).add(current));

				if (te.isCrafting && (te.output != null)) {
					if (RandUtil.nextInt(count > 0 && count / 4 > 0 ? count / 4 : 1) == 0) {
						LibParticles.CRAFTING_ALTAR_CLUSTER_SUCTION(te.getWorld(), new Vec3d(te.getPos()).addVector(0.5, 0.75, 0.5), new InterpBezier3D(current, new Vec3d(0, 0, 0)));
					}
				}
			}

			GlStateManager.pushMatrix();
			GlStateManager.translate(x + 0.5 + current.x, y + 0.5 + current.y, z + 0.5 + current.z);
			GlStateManager.scale(0.3, 0.3, 0.3);
			GlStateManager.rotate((cluster.tick) + ClientTickHandler.getPartialTicks(), 0, 1, 0);
			Minecraft.getMinecraft().getRenderItem().renderItem(cluster.stack, TransformType.NONE);
			GlStateManager.popMatrix();
			//Minecraft.getMinecraft().player.sendChatMessage((cluster.stack.hashCode()) / 100000000.0 + "");
		}

		//if (!manager.isManaEmpty() && te.isCrafting && (te.output != null)) {
		//	LibParticles.CRAFTING_ALTAR_HELIX(te.getWorld(), new Vec3d(te.getPos()).addVector(0.5, 0.25, 0.5));
		//}

		if (!te.isCrafting && (te.output != null)) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + 0.5, y + 1, z + 0.5);
			GlStateManager.scale(0.4, 0.4, 0.4);
			GlStateManager.rotate(te.tick, 0, 1, 0);
			Minecraft.getMinecraft().getRenderItem().renderItem(te.output, TransformType.NONE);
			GlStateManager.popMatrix();
		} else if (!manager.isManaEmpty() && RandUtil.nextInt(4) == 0) {
			LibParticles.CRAFTING_ALTAR_IDLE(te.getWorld(), new Vec3d(te.getPos()).addVector(0.5, 0.7, 0.5));
		}
	}
}
