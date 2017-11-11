package com.teamwizardry.wizardry.common.module.effects;

import com.teamwizardry.wizardry.api.spell.SpellData;
import com.teamwizardry.wizardry.api.spell.attribute.Attributes;
import com.teamwizardry.wizardry.api.spell.module.Module;
import com.teamwizardry.wizardry.api.spell.module.ModuleEffect;
import com.teamwizardry.wizardry.api.spell.module.ModuleModifier;
import com.teamwizardry.wizardry.api.spell.module.RegisterModule;
import com.teamwizardry.wizardry.api.util.BlockUtils;
import com.teamwizardry.wizardry.api.util.RandUtil;
import com.teamwizardry.wizardry.client.fx.LibParticles;
import com.teamwizardry.wizardry.common.module.modifiers.ModuleModifierExtendTime;
import com.teamwizardry.wizardry.common.module.modifiers.ModuleModifierIncreaseAOE;
import com.teamwizardry.wizardry.init.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.awt.*;

import static com.teamwizardry.wizardry.api.spell.SpellData.DefaultKeys.*;

/**
 * Created by LordSaad.
 */
@RegisterModule
public class ModuleEffectBurn extends ModuleEffect {

	@Nonnull
	@Override
	public String getID() {
		return "effect_burn";
	}

	@Nonnull
	@Override
	public String getReadableName() {
		return "Burn";
	}

	@Nonnull
	@Override
	public String getDescription() {
		return "Will burn the target block or entity.";
	}

	@Override
	public ModuleModifier[] applicableModifiers() {
		return new ModuleModifier[]{new ModuleModifierIncreaseAOE(), new ModuleModifierExtendTime()};
	}

	@Override
	public boolean run(@Nonnull SpellData spell) {
		// FIXME: 11/11/2017
		World world = spell.world;
		Entity targetEntity = spell.getData(ENTITY_HIT);
		BlockPos targetPos = spell.getData(BLOCK_HIT);
		Entity caster = spell.getData(CASTER);
		EnumFacing facing = spell.getData(FACE_HIT);

		double strength = getModifier(spell, Attributes.AREA, 1, 16) / 2.0;
		double time = getModifier(spell, Attributes.DURATION, 100, 1000);

		if (!tax(this, spell)) return false;

		if (targetEntity != null) {
			targetEntity.setFire((int) time);
			world.playSound(null, targetEntity.getPosition(), ModSounds.FIRE, SoundCategory.NEUTRAL, 1, 1);
		}

		if (targetPos != null) {
			for (int x = (int) strength; x >= -strength; x--)
				for (int y = (int) strength; y >= -strength; y--)
					for (int z = (int) strength; z >= -strength; z--) {
						BlockPos pos = targetPos.add(x, y, z);
						double dist = pos.getDistance(targetPos.getX(), targetPos.getY(), targetPos.getZ());
						if (dist > strength) continue;
						if (facing != null) {
							if (!world.isAirBlock(pos.offset(facing))) return true;
							BlockUtils.placeBlock(world, pos.offset(facing), Blocks.FIRE.getDefaultState(), caster instanceof EntityPlayer ? (EntityPlayerMP) caster : null);
							world.playSound(null, targetPos, ModSounds.FIRE, SoundCategory.NEUTRAL, 0.5f, RandUtil.nextFloat());
						} else for (EnumFacing face : EnumFacing.VALUES) {
							if (world.isAirBlock(pos.offset(face)) || world.getBlockState(pos.offset(face)).getBlock() == Blocks.SNOW_LAYER) {
								BlockUtils.placeBlock(world, pos.offset(face), Blocks.AIR.getDefaultState(), caster instanceof EntityPlayer ? (EntityPlayerMP) caster : null);
								world.playSound(null, targetPos, ModSounds.FIRE, SoundCategory.NEUTRAL, 0.5f, RandUtil.nextFloat());
							}
						}
					}
		}
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void runClient(@Nonnull SpellData spell) {
		World world = spell.world;
		Vec3d position = spell.getData(TARGET_HIT);

		if (position == null) return;

		Color color = getPrimaryColor();
		if (RandUtil.nextBoolean()) color = getSecondaryColor();

		LibParticles.EFFECT_BURN(world, position, color);
	}

	@Nonnull
	@Override
	public Module copy() {
		return cloneModule(new ModuleEffectBurn());
	}
}
