package forestry.core.models;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public abstract class ModelBlockCached<B extends Block, K> extends ModelBlockDefault<B, K> {
    private static final Set<ModelBlockCached> CACHE_PROVIDERS = new HashSet<>();

    private final Cache<K, IBakedModel> inventoryCache;
    private final Cache<K, IBakedModel> worldCache;

    public static void clear() {
        for (ModelBlockCached modelBlockCached : CACHE_PROVIDERS) {
            modelBlockCached.worldCache.invalidateAll();
            modelBlockCached.inventoryCache.invalidateAll();
        }
    }

    protected ModelBlockCached(@Nonnull Class<B> blockClass) {
        super(blockClass);

        worldCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();
        inventoryCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();

        CACHE_PROVIDERS.add(this);
    }

    @Override
    protected IBakedModel getModel(@Nonnull IBlockState state) {
    	if(state == null){
    		return null;
    	}
        K key = getWorldKey(state);
        if (key == null) {
            return null;
        }

        IBakedModel model = worldCache.getIfPresent(key);
        if (model == null) {
            model = bakeModel(state, key);
            if (model != null) {
                worldCache.put(key, model);
            }
        }
        return model;
    }

    @Override
    protected IBakedModel getModel(ItemStack stack, World world) {
        K key = getInventoryKey(stack);
        if (key == null) {
            return null;
        }

        IBakedModel model = inventoryCache.getIfPresent(key);
        if (model == null) {
            model = bakeModel(stack, world, key);
            inventoryCache.put(key, model);
        }
        return model;
    }
}
