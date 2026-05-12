package maxhyper.dtterralith;

import com.dtteam.dynamictrees.api.DynamicTreesAddonEntrypoint;
import com.dtteam.dynamictrees.api.cell.CellKit;
import com.dtteam.dynamictrees.systems.genfeature.GenFeature;
import com.dtteam.dynamictrees.systems.growthlogic.GrowthLogicKit;
import com.dtteam.dynamictrees.tree.species.Species;
import maxhyper.dtterralith.cellkits.DTTerralithCellKits;
import maxhyper.dtterralith.genfeatures.DTTerralithGenFeatures;
import maxhyper.dtterralith.growthlogic.DTTGrowthLogicKits;
import maxhyper.dtterralith.trees.PoplarSpecies;

public class DynamicTreesTerralithAddon implements DynamicTreesAddonEntrypoint {

	@Override
	public void onDynamicTreesPreSetup() {
		DynamicTreesAddonEntrypoint.setupAddon(DynamicTreesTerralith.MOD_ID);

		Species.REGISTRY.registerType(DynamicTreesTerralith.location("poplar"), PoplarSpecies.TYPE);

		CellKit.REGISTRY.registerAll(
				DTTerralithCellKits.SPARSE,
				DTTerralithCellKits.POPLAR,
				DTTerralithCellKits.WIDE_DARK_OAK
		);
		GrowthLogicKit.REGISTRY.registerAll(
				DTTGrowthLogicKits.TALL_DECIDUOUS,
				DTTGrowthLogicKits.TWISTING_TREE,
				DTTGrowthLogicKits.VARIATE_HEIGHT,
				DTTGrowthLogicKits.POPLAR,
				DTTGrowthLogicKits.BAOBAB
		);
		GenFeature.REGISTRY.register(DTTerralithGenFeatures.RANDOM_STRIPPED_BRANCHES);
	}
}
