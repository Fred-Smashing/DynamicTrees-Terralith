package maxhyper.dtterralith.genfeatures;

import maxhyper.dtterralith.DynamicTreesTerralith;
import com.dtteam.dynamictrees.systems.genfeature.GenFeature;

public class DTTerralithGenFeatures {

    public static final GenFeature RANDOM_STRIPPED_BRANCHES = new RandomStrippedBranches(DynamicTreesTerralith.location("random_stripped_branches"));

}
