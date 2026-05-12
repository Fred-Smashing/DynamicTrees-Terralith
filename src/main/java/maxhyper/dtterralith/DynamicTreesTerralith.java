package maxhyper.dtterralith;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import maxhyper.dtterralith.registry.DTTRegistries;
import net.fabricmc.api.ModInitializer;
import net.neoforged.fml.config.ModConfig;
import net.minecraft.resources.Identifier;

public class DynamicTreesTerralith implements ModInitializer {

	public static final String MOD_ID = "dtterralith";

	@Override
	public void onInitialize() {
		ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.COMMON, DTTConfigs.GENERAL_SPEC);

		DTTRegistries.setup();
	}

	public static Identifier location(String name) {
		return Identifier.fromNamespaceAndPath(MOD_ID, name);
	}
}
