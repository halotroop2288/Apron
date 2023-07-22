package io.github.betterthanupdates.apron.stapi.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.modificationstation.stationapi.impl.client.arsenic.renderer.render.ArsenicItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import io.github.betterthanupdates.apron.stapi.ApronStAPICompat;

@Mixin(ArsenicItemRenderer.class)
public class ArsenicItemRendererMixin {
	@Redirect(method = "renderVanilla", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItemTexture()I"))
	private int apron$fixItemTextureIndex(ItemStack instance) {
		int oldIndex = instance.getItemTexture();
		Item item = instance.getItem();

		return ApronStAPICompat.fixItemTexture(oldIndex, item);
	}

	@ModifyVariable(method = "renderItemOnGui(Lnet/minecraft/client/render/TextRenderer;Lnet/minecraft/client/texture/TextureManager;IIIII)V",
			argsOnly = true, at = @At("HEAD"), ordinal = 2)
	private int apron$fixItemTextureIndex(int old, @Local(argsOnly = true, ordinal = 0) int id) {
		Item item = Item.byId[id];

		return ApronStAPICompat.fixItemTexture(old, item);
	}
}
