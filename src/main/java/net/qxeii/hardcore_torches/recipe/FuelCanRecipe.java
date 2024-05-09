package net.qxeii.hardcore_torches.recipe;

import com.google.gson.JsonObject;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.item.FuelCanItem;

public class FuelCanRecipe extends ShapelessRecipe {
	final int fuelAmount;
	final int configType;

	public FuelCanRecipe(Identifier id, String group, ItemStack output, DefaultedList<Ingredient> input, int fuelAmount,
			int configType) {
		super(id, group, CraftingRecipeCategory.EQUIPMENT, output, input);
		this.fuelAmount = fuelAmount;
		this.configType = configType;
	}

	public RecipeSerializer<?> getSerializer() {
		return Mod.FUEL_RECIPE_SERIALIZER;
	}

	@Override
	public boolean matches(RecipeInputInventory recipeInputInventory, World world) {
		if (configType == 0 && !Mod.config.enableCanRefillWithFuel)
			return false;
		if (configType == 1 && !Mod.config.enableCanRefillWithCoal)
			return false;
		return super.matches(recipeInputInventory, world);
	}

	@Override
	public ItemStack craft(RecipeInputInventory grid, DynamicRegistryManager dynamicRegistryManager) {
		int startFuel;
		for (int i = 0; i < grid.size(); ++i) {
			ItemStack itemstack = grid.getStack(i);

			if (itemstack.getItem() instanceof FuelCanItem) {
				FuelCanItem can = (FuelCanItem) itemstack.getItem();

				startFuel = can.getFuel(itemstack);
				int fuel = (int) (fuelAmount * Mod.config.fuelCanRecipeMultiplier);
				System.out.println("REC " + Mod.config.fuelRecipeOverride);
				if (Mod.config.fuelRecipeOverride > -1) {
					fuel = Mod.config.fuelRecipeOverride;
					System.out.println("REC " + fuel);
				}

				return FuelCanItem.setFuel(itemstack.copy(), startFuel + fuel);
			}
		}

		return ItemStack.EMPTY;
	}

	public static class Serializer implements RecipeSerializer<FuelCanRecipe> {
		public Serializer() {
		}

		private static final Identifier NAME = new Identifier("hardcore_torches", "fuel_can");

		@Override
		public FuelCanRecipe read(Identifier resourceLocation, JsonObject json) {
			ShapelessRecipe recipe = ShapelessRecipe.Serializer.SHAPELESS.read(resourceLocation, json);
			int fuel = json.get("fuel").getAsInt();
			int configType = json.get("config_type").getAsInt();

			return new FuelCanRecipe(recipe.getId(), recipe.getGroup(), recipe.getOutput(DynamicRegistryManager.EMPTY),
					recipe.getIngredients(), fuel, configType);
		}

		@Override
		public FuelCanRecipe read(Identifier resourceLocation, PacketByteBuf friendlyByteBuf) {
			ShapelessRecipe recipe = ShapelessRecipe.Serializer.SHAPELESS.read(resourceLocation, friendlyByteBuf);
			int fuelValue = friendlyByteBuf.readVarInt();
			int configType = friendlyByteBuf.readVarInt();

			return new FuelCanRecipe(recipe.getId(), recipe.getGroup(), recipe.getOutput(DynamicRegistryManager.EMPTY),
					recipe.getIngredients(), fuelValue, configType);
		}

		@Override
		public void write(PacketByteBuf friendlyByteBuf, FuelCanRecipe fuelCanRecipe) {
			ShapelessRecipe rec = new ShapelessRecipe(fuelCanRecipe.getId(), fuelCanRecipe.getGroup(),
					CraftingRecipeCategory.EQUIPMENT, fuelCanRecipe.getOutput(DynamicRegistryManager.EMPTY),
					fuelCanRecipe.getIngredients());
			ShapelessRecipe.Serializer.SHAPELESS.write(friendlyByteBuf, rec);

			friendlyByteBuf.writeVarInt(fuelCanRecipe.fuelAmount);
			friendlyByteBuf.writeVarInt(fuelCanRecipe.configType);
		}
	}
}
