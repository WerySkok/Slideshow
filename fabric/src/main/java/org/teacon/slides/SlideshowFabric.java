package org.teacon.slides;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.teacon.slides.mappings.BlockEntityMapper;

public class SlideshowFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		Slideshow.init(SlideshowFabric::registerBlock, SlideshowFabric::registerBlockEntityType);
	}

	private static void registerBlock(String path, RegistryObject<Block> block) {
		Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(Slideshow.ID, path), block.get());
	}

	private static void registerBlock(String path, RegistryObject<Block> block, CreativeModeTab itemGroup) {
		registerBlock(path, block);
		BlockItem blockItem = new BlockItem(block.get(), new Item.Properties());
		Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(Slideshow.ID, path), blockItem);
		ItemGroupEvents.modifyEntriesEvent(itemGroup).register(entries -> entries.accept(blockItem));
	}

	private static void registerBlockEntityType(String path, RegistryObject<? extends BlockEntityType<? extends BlockEntityMapper>> blockEntityType) {
		Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, new ResourceLocation(Slideshow.ID, path), blockEntityType.get());
	}
}
