package com.teamwizardry.wizardry.client.gui.book;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.teamwizardry.librarianlib.LibrarianLib;
import com.teamwizardry.librarianlib.client.core.ClientTickHandler;
import com.teamwizardry.librarianlib.client.gui.GuiBase;
import com.teamwizardry.librarianlib.client.gui.GuiComponent;
import com.teamwizardry.librarianlib.client.gui.components.ComponentSprite;
import com.teamwizardry.librarianlib.client.gui.components.ComponentVoid;
import com.teamwizardry.librarianlib.client.gui.mixin.gl.GlMixin;
import com.teamwizardry.librarianlib.client.sprite.Sprite;
import com.teamwizardry.librarianlib.client.sprite.Texture;
import com.teamwizardry.librarianlib.common.util.math.Vec2d;
import com.teamwizardry.wizardry.Wizardry;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by LordSaad.
 */
public class BookGui extends GuiBase {

	public static Texture SPRITE_SHEET = new Texture(new ResourceLocation(Wizardry.MODID, "textures/gui/book/book.png"));
	private static Sprite background = SPRITE_SHEET.getSprite("background", 145, 179);
	public ComponentVoid mainIndex;
	private HashMap<ComponentVoid, Pair<Long, Boolean>> sliders = new HashMap<>();

	public BookGui() {
		super(145, 179);
		ComponentSprite componentBackground = new ComponentSprite(background, 0, 0);
		getMainComponents().add(componentBackground);

		mainIndex = new ComponentVoid(0, 0, background.getWidth(), background.getHeight());
		componentBackground.add(mainIndex);

		String langname = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
		InputStream stream;
		String path;
		try {
			stream = LibrarianLib.PROXY.getResource(Wizardry.MODID, "documentation/" + langname + "/index.json");
			path = "documentation/" + langname;
		} catch (Throwable e) {
			stream = LibrarianLib.PROXY.getResource(Wizardry.MODID, "documentation/en_US/index.json");
			path = "documentation/en_US";
		}

		if (stream != null) {
			InputStreamReader reader = new InputStreamReader(stream);
			JsonElement json = new JsonParser().parse(reader);

			if (json.isJsonObject() && json.getAsJsonObject().has("index")) {
				JsonArray array = json.getAsJsonObject().getAsJsonArray("index");
				int i = 0;
				for (JsonElement element : array) {
					if (element.isJsonObject()) {
						JsonObject chunk = element.getAsJsonObject();
						if (chunk.has("icon") && chunk.has("text") && chunk.has("link") && chunk.get("icon").isJsonPrimitive() && chunk.get("text").isJsonPrimitive() && chunk.get("link").isJsonPrimitive()) {
							ComponentVoid category = new ComponentVoid(15 + ((32 + 5) * i), 15, 32, 32);
							Sprite icon = new Sprite(new ResourceLocation(chunk.get("icon").getAsString()));
							category.BUS.hook(GuiComponent.PostDrawEvent.class, postDrawEvent -> {
								GlStateManager.pushMatrix();
								GlStateManager.enableAlpha();
								GlStateManager.enableBlend();
								if (!postDrawEvent.getComponent().getMouseOver())
									GlStateManager.color(0, 0, 0);
								else GlStateManager.color(0, 0.5f, 1);
								icon.getTex().bind();
								icon.draw((int) ClientTickHandler.getPartialTicks(), category.getPos().getXi(), category.getPos().getYi(), 32, 32);
								GlStateManager.popMatrix();
							});
							hookSlider(category, "TEEEEEEEEEEEEEEEEEST IIIIIIIIIIIIIIIIING");
							final String finalPath = path + chunk.get("link").getAsString();
							category.BUS.hook(GuiComponent.MouseClickEvent.class, mouseClickEvent -> {
								Page page = new Page(this, finalPath, background.getWidth(), background.getHeight(), 0);
								mainIndex.setVisible(false);
								mainIndex.setEnabled(false);
								getMainComponents().add(page.component);
							});
							mainIndex.add(category);
							i++;
						}
					}
				}
			}
		}
	}

	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}

	public void hookSlider(GuiComponent<?> component, String text) {
		Slider slider = new Slider(text);
		slider.component.setPos(new Vec2d(-component.getPos().getX(), component.getPos().getY()));
		slider.component.setEnabled(false);
		component.add(slider.component);
		GlMixin.INSTANCE.transform(slider.component).setValue(new Vec3d(slider.component.getPos().getX(), slider.component.getPos().getY(), -10));

		component.BUS.hook(GuiComponent.ComponentTickEvent.class, componentTickEvent -> {
			float t = -1, tmax = 1;
			float finalLoc = -130, initialLoc = 0;
			float x;
			if (component.getMouseOver()) {
				for (Object tag : component.getTags()) {
					Minecraft.getMinecraft().player.sendChatMessage(tag + "");
					if (tag instanceof String && ((String) tag).startsWith("t:")) {
						t = Float.parseFloat(((String) tag).split(":")[1]);
						if (t > 0) {
							component.removeTag(tag);
							component.addTag("t:" + (t - 0.1));
						}
						break;
					}
				}
				if (t == -1) component.addTag("t:" + tmax);
				if (t <= 0) return;

				x = (finalLoc - initialLoc) * MathHelper.cos((float) (Math.PI / 2 * t / tmax)) + initialLoc;

			} else {
				for (Object tag : component.getTags()) {
					Minecraft.getMinecraft().player.sendChatMessage(tag + "");
					if (tag instanceof String && ((String) tag).startsWith("t:")) {
						t = Float.parseFloat(((String) tag).split(":")[1]);
						if (t < tmax) {
							component.removeTag(tag);
							component.addTag("t:" + (t + 0.1));
						}
						break;
					}
				}
				if (t == -1) component.addTag("t:" + 0);
				if (t > tmax) return;


				x = (initialLoc - finalLoc) * MathHelper.sin((float) (Math.PI / 2 * t / tmax)) + finalLoc;
			}

			slider.component.setPos(new Vec2d(x, component.getPos().getY()));
		});
	}
}
