package io.github.betterthanupdates.babricated.mixin;

import static io.github.betterthanupdates.babricated.BabricatedForge.fabricModsLoaded;
import static io.github.betterthanupdates.babricated.BabricatedForge.rmlModsLoaded;
import static io.github.betterthanupdates.babricated.BabricatedForge.versionString;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.InGameHud;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends GuiElement {
	@Shadow
	private Minecraft client;

	@ModifyConstant(method = "render(FZII)V", constant = @Constant(stringValue = "Minecraft Beta 1.7.3 ("))
	private String babricated$replaceVersionString(String constant) {
		return versionString(constant);
	}

	@Inject(method = "render(FZII)V", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/util/math/MathHelper;floor(D)I"))
	private void babricated$addDebugLine(float bl, boolean i, int j, int par4, CallbackInfo ci) {
		this.drawTextWithShadow(this.client.textRenderer,
				String.format("%s, %s", rmlModsLoaded(), fabricModsLoaded()), 2, 98, 0xE0_E0_E0);
	}
}
