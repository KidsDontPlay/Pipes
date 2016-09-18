package mrriegel.pipes;

import mrriegel.pipes.tile.TileItemPipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class TileItemPipeRender extends TileEntitySpecialRenderer<TileItemPipe> {

	@Override
	public void renderTileEntityAt(TileItemPipe te, double x, double y, double z, float partialTicks, int destroyStage) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		Minecraft mc = Minecraft.getMinecraft();
		RenderItem itemRenderer = mc.getRenderItem();
		double factor = Minecraft.getDebugFPS() / 20d;
		// if (!tr.blocked && !mc.isGamePaused() &&
		// mc.theWorld.getChunkFromBlockCoords(tr.rec.getLeft()).isLoaded()) {
		// tr.current = tr.current.add(tr.getVec().scale((te.getSpeed() /
		// factor) / tr.getVec().lengthVector()));
		// }
		//
		GlStateManager.translate(.5, .5, .5);

		ItemStack stack = new ItemStack(Items.GOLDEN_APPLE);
		stack = new ItemStack(Blocks.BEDROCK);
		EntityItem ei = new EntityItem(mc.theWorld, 0, 0, 0, stack);
		ei.hoverStart = 0;

		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();

		// GuiUtils.drawGradientRect(0, 0, 0, 1, 1, Color.red.getRGB(),
		// Color.red.getRGB());

		float rotation = (float) (720.0 * (System.currentTimeMillis() & 0x3FFFL) / 0x3FFFL);
		rotation = 0;
		GlStateManager.rotate(rotation, 0.0F, 1.0F, 0);
		GlStateManager.scale(0.4F, 0.4F, 0.4F);
		GlStateManager.pushAttrib();
		RenderHelper.enableStandardItemLighting();
		// itemRenderer.renderItem(ei.getEntityItem(),
		// ItemCameraTransforms.TransformType.FIXED);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.popAttrib();

		GlStateManager.enableLighting();
		GlStateManager.popMatrix();

		GlStateManager.popMatrix();
	}

}
