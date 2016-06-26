package com.teamwizardry.wizardry.gui.book.pages;

import com.teamwizardry.wizardry.Wizardry;
import com.teamwizardry.wizardry.api.Constants;
import com.teamwizardry.wizardry.gui.book.Tippable;
import com.teamwizardry.wizardry.gui.book.util.DataNode;
import com.teamwizardry.wizardry.gui.book.util.PageRegistry;
import com.teamwizardry.wizardry.gui.book.util.PathUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public abstract class GuiPageCommon extends Tippable {

    public String path;
    public int page;
    public int viewWidth, viewHeight, viewLeft, viewTop;
    protected GuiScreen parent;
    protected int screenScale;
    protected DataNode data;
    protected DataNode globalData;

    { /* helpers */ }

    public GuiPageCommon(GuiScreen parent, DataNode data, DataNode globalData, String path, int page) {
        this.data = data;
        this.globalData = data;
        this.parent = parent;
        this.path = path;
        this.page = page;
        this.viewWidth = 115;
        this.viewHeight = 154;
        if (globalData.get("title").isString()) {
            this.title = globalData.get("title").asString();
        }
        setHasNavReturn(true);
        setHasNavNext(data.get("hasNext").exists());
        setHasNavPrev(data.get("hasPrev").exists());
    }
    public void mouseClickedPage(int mouseX, int mouseY, int button) {}
    public void mouseReleasedPage(int mouseX, int mouseY, int button) {}
    public void mouseClickMovePage(int mouseX, int mouseY, int button, long timeSinceLastClick) {}
    
    public abstract void drawPage(int mouseX, int mouseY, float partialTicks);

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        mc.fontRendererObj.setUnicodeFlag(true);

        GlStateManager.pushMatrix();
        viewLeft = left + 15;
        viewTop = top + 12;
        GlStateManager.translate(viewLeft, viewTop, 100);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(viewLeft * screenScale, mc.displayHeight - (viewTop + viewHeight) * screenScale,
                viewWidth * screenScale, viewHeight * screenScale);
        drawPage(mouseX - viewLeft, mouseY - viewTop, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.popMatrix();

        mc.fontRendererObj.setUnicodeFlag(false);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    	super.mouseClicked(mouseX, mouseY, mouseButton);
    	mouseClickedPage(mouseX - viewLeft, mouseY - viewTop, mouseButton);
    }
    
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
    	super.mouseReleased(mouseX, mouseY, state);
    	mouseReleasedPage(mouseX - viewLeft, mouseY - viewTop, state);
    }
    
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
    	super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    	mouseClickMovePage(mouseX - viewLeft, mouseY - viewTop, clickedMouseButton, timeSinceLastClick);
    }
    
    @Override
    public void initGui() {
        super.initGui();
        screenScale = new ScaledResolution(mc).getScaleFactor();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == Constants.GuiButtons.NAV_BAR_INDEX) {
            if (parent == null) {
                String indexPath = PathUtils.resolve(PathUtils.parent(path), "index");
                if (!indexPath.equals(path))
                    parent = PageRegistry.construct(null, indexPath, 0); // parent is null because otherwise pressing back goes to the child gui
                if (parent == null) {
                    parent = PageRegistry.construct(null, "/", 0); // see above ^
                }
            }
            mc.displayGuiScreen(parent);
        }
        if (button.id == Constants.GuiButtons.NAV_BAR_NEXT) {
            openPage(path, page + 1);
        }
        if (button.id == Constants.GuiButtons.NAV_BAR_BACK) {
            openPage(path, page - 1);
        }
    }

    public void openPageRelative(String path, int page) {
        openPage(PathUtils.resolve(PathUtils.parent(this.path), path), page);
    }

    public void openPage(String path, int page) {
        mc.displayGuiScreen(PageRegistry.construct(this.path.equals(path) ? this.parent : this, path, page));
    }

    public ResourceLocation pageResource(String path) {
        return new ResourceLocation(Wizardry.MODID, PathUtils.resolve("textures/" + PathUtils.resolve(PathUtils.parent(this.path), path)));
    }
}