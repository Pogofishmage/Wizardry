package com.teamwizardry.wizardry.common.item.halos;

import baubles.api.BaubleType;
import com.teamwizardry.librarianlib.features.base.item.ItemModBauble;
import com.teamwizardry.wizardry.api.ConfigValues;
import com.teamwizardry.wizardry.api.capability.CapManager;
import com.teamwizardry.wizardry.api.item.IFakeHalo;
import com.teamwizardry.wizardry.api.item.IHalo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Created by Saad on 8/30/2016.
 */
@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")
public class ItemCreativeHaloBauble extends ItemModBauble implements IFakeHalo, IHalo {

	public ItemCreativeHaloBauble() {
		super("halo_creative");
		setMaxStackSize(1);
	}

	@Override
	public void onWornTick(@NotNull ItemStack stack, @NotNull EntityLivingBase player) {
		CapManager manager = new CapManager(player).setManualSync(true);

		manager.setMaxMana(ConfigValues.creativeHaloBufferSize);
		manager.setMaxBurnout(ConfigValues.creativeHaloBufferSize);
		manager.setMana(ConfigValues.creativeHaloBufferSize);
		manager.setBurnout(0);

		if (manager.isSomethingChanged())
			manager.sync();
	}

	@Nonnull
	@Optional.Method(modid = "baubles")
	@Override
	public BaubleType getBaubleType(@NotNull ItemStack itemStack) {
		return BaubleType.HEAD;
	}
}
