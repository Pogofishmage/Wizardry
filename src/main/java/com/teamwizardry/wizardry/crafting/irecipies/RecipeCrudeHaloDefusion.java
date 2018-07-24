package com.teamwizardry.wizardry.crafting.irecipies;

import com.teamwizardry.librarianlib.features.helpers.ItemNBTHelper;
import com.teamwizardry.wizardry.api.item.halo.HaloInfusionItem;
import com.teamwizardry.wizardry.api.item.halo.HaloInfusionItemRegistry;
import com.teamwizardry.wizardry.init.ModItems;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;

/**
 * Created by Demoniaque on 8/30/2016.
 */
public class RecipeCrudeHaloDefusion extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	@Override
	public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World worldIn) {

		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack.getItem() == ModItems.FAKE_HALO) return true;
		}

		return false;
	}

	@Override
	@Nonnull
	public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
		return new ItemStack(ModItems.FAKE_HALO);
	}

	@Override
	public boolean canFit(int width, int height) {
		return true;
	}

	@Override
	@Nonnull
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(@Nonnull InventoryCrafting inv) {
		NonNullList<ItemStack> remainingItems = ForgeHooks.defaultRecipeGetRemainingItems(inv);

		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack.getItem() == ModItems.FAKE_HALO) {

				NBTTagList slots = ItemNBTHelper.getList(stack, "slots", NBTTagString.class);
				if (slots == null || slots.tagCount() < HaloInfusionItemRegistry.getItems().size()) {
					slots = new NBTTagList();

					for (int j = 0; j < HaloInfusionItemRegistry.getItems().size(); j++) {
						slots.appendTag(new NBTTagString(HaloInfusionItemRegistry.EMPTY.getNbtName()));
					}
				}

				for (int j = 0; j < slots.tagCount(); j++) {
					String string = slots.getStringTagAt(j);
					HaloInfusionItem infusionItem = HaloInfusionItemRegistry.getItemFromName(string);
					if (infusionItem == HaloInfusionItemRegistry.EMPTY) continue;
					remainingItems.add(i, infusionItem.getStack());
				}
				break;
			}
		}

		return remainingItems;
	}
}
