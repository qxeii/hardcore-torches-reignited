package net.qxeii.hardcore_torches.recipe;

import com.google.gson.JsonObject;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.qxeii.hardcore_torches.Mod;

public class CandleRecipe extends ShapedRecipe {

	public CandleRecipe(Identifier id, String group, int width, int height, DefaultedList<Ingredient> input,
			ItemStack output) {
		super(id, group, CraftingRecipeCategory.EQUIPMENT, width, height, input, output);
	}

	public RecipeSerializer<?> getSerializer() {
		return Mod.CANDLE_RECIPE_SERIALIZER;
	}

	@Override
	public ItemStack craft(RecipeInputInventory recipeInputInventory, DynamicRegistryManager dynamicRegistryManager) {
		ItemStack stack = getOutput(DynamicRegistryManager.EMPTY);
		Item stackItem = stack.getItem();

		return new ItemStack(stackItem, Mod.config.candleCraftAmount);
	}

	public static class Serializer implements RecipeSerializer<CandleRecipe> {
		public Serializer() {
		}

		private static final Identifier NAME = new Identifier("hardcore_torches", "candle");

		@Override
		public CandleRecipe read(Identifier resourceLocation, JsonObject json) {
			ShapedRecipe recipe = ShapedRecipe.Serializer.SHAPED.read(resourceLocation, json);

			return new CandleRecipe(recipe.getId(), recipe.getGroup(), recipe.getWidth(), recipe.getHeight(),
					recipe.getIngredients(), recipe.getOutput(DynamicRegistryManager.EMPTY));
		}

		@Override
		public CandleRecipe read(Identifier resourceLocation, PacketByteBuf friendlyByteBuf) {
			ShapedRecipe recipe = ShapedRecipe.Serializer.SHAPED.read(resourceLocation, friendlyByteBuf);

			return new CandleRecipe(recipe.getId(), recipe.getGroup(), recipe.getWidth(), recipe.getHeight(),
					recipe.getIngredients(), recipe.getOutput(DynamicRegistryManager.EMPTY));
		}

		@Override
		public void write(PacketByteBuf friendlyByteBuf, CandleRecipe torchRecipe) {
			ShapedRecipe rec = new ShapedRecipe(torchRecipe.getId(), torchRecipe.getGroup(),
					CraftingRecipeCategory.EQUIPMENT, torchRecipe.getWidth(), torchRecipe.getHeight(),
					torchRecipe.getIngredients(), torchRecipe.getOutput(DynamicRegistryManager.EMPTY));

			ShapedRecipe.Serializer.SHAPED.write(friendlyByteBuf, rec);
		}
	}
}
