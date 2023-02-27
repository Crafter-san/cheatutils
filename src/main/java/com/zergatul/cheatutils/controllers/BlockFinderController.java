package com.zergatul.cheatutils.controllers;

import com.mojang.datafixers.util.Pair;
import com.zergatul.cheatutils.configs.BlockTracerConfig;
import com.zergatul.cheatutils.interfaces.LevelChunkMixinInterface;
import com.zergatul.cheatutils.utils.Dimension;
import com.zergatul.cheatutils.utils.ThreadLoadCounter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockFinderController {

    public static final BlockFinderController instance = new BlockFinderController();

    // all modification to blocks are done in eventLoop thread
    public final Map<Block, Set<BlockPos>> blocks = new ConcurrentHashMap<>();

    private final Logger logger = LogManager.getLogger(BlockFinderController.class);
    private final Object loopWaitEvent = new Object();
    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private final ThreadLoadCounter counter = new ThreadLoadCounter();
    private Thread eventLoop;

    private BlockFinderController() {
        ChunkController.instance.addOnChunkLoadedHandler(this::scanChunk);
        ChunkController.instance.addOnChunkUnLoadedHandler(this::unloadChunk);
        ChunkController.instance.addOnBlockChangedHandler(this::handleBlockUpdate);

        restartBackgroundThread(null);
    }

    public void restart() {
        restartBackgroundThread(() -> {
            clear();
            for (Pair<Dimension, WorldChunk> pair: ChunkController.instance.getLoadedChunks()) {
                scanChunk(pair.getSecond());
            }
        });
    }

    private void restartBackgroundThread(Runnable beforeThreadStart) {
        /* stop */
        if (eventLoop != null) {
            queue.clear();
            synchronized (loopWaitEvent) {
                loopWaitEvent.notify();
            }
            eventLoop.interrupt();
        }

        eventLoop = null;

        /* start */

        eventLoop = new Thread(() -> {
            boolean first = true;
            try {
                while (true) {
                    counter.startWait();
                    if (first) {
                        first = false;
                    } else {
                        synchronized (loopWaitEvent) {
                            loopWaitEvent.wait();
                        }
                    }
                    counter.startLoad();
                    while (queue.size() > 0) {
                        Runnable process = queue.remove();
                        process.run();
                        Thread.yield();
                    }
                }
            }
            catch (InterruptedException e) {
                // do nothing
            }
            catch (Throwable e) {
                logger.error("BlockFinder scan thread crash.", e);
            }
            finally {
                counter.dispose();
            }
        }, "BlockFinderScanThread");

        if (beforeThreadStart != null) {
            beforeThreadStart.run();
        }

        eventLoop.start();
    }

    public void clear() {
        addToQueue(() -> {
            for (Block block: blocks.keySet()) {
                blocks.put(block, ConcurrentHashMap.newKeySet());
            }
        });
    }

    public void addConfig(BlockTracerConfig config) {
        addToQueue(() -> {
            blocks.put(config.block, ConcurrentHashMap.newKeySet());
            scan(config.block);
        });
    }

    public void removeConfig(BlockTracerConfig config) {
        addToQueue(() -> blocks.remove(config.block));
    }

    public void removeAllConfigs() {
        addToQueue(blocks::clear);
    }

    public String getThreadState() {
        Thread thread = eventLoop;
        if (thread != null) {
            return eventLoop.getState().toString();
        } else {
            return null;
        }
    }

    public double getScanningThreadLoadPercent() {
        return 100d * counter.getLoad(1);
    }

    public int getScanningQueueCount() {
        return queue.size();
    }

    private void scanChunk(WorldChunk chunk) {
        addToQueue(() -> {
            LevelChunkMixinInterface mixinChunk = (LevelChunkMixinInterface) chunk;
            Dimension dimension = mixinChunk.getDimension();
            if (chunk.getStatus() != ChunkStatus.FULL) {
                // re-add chunk to the queue
                // but check if this chunk is still valid
                if (ChunkController.instance.getLoadedChunks().stream().anyMatch(p -> p.getFirst() == dimension && p.getSecond() == chunk)) {
                    scanChunk(chunk);
                }
                return;
            }

            int minY = dimension.getMinY();
            int xc = chunk.getPos().x << 4;
            int zc = chunk.getPos().z << 4;
            var pos = new BlockPos.Mutable();
            for (int x = 0; x < 16; x++) {
                pos.setX(xc | x);
                for (int z = 0; z < 16; z++) {
                    pos.setZ(zc | z);
                    int height = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE).get(x, z);
                    for (int y = minY; y <= height; y++) {
                        pos.setY(y);
                        BlockState state = chunk.getBlockState(pos);
                        checkBlock(state, pos);
                    }
                }
            }
        });
    }

    private void unloadChunk(WorldChunk chunk) {
        addToQueue(() -> {
            int cx = chunk.getPos().x;
            int cz = chunk.getPos().z;
            for (Set<BlockPos> set: blocks.values()) {
                set.removeIf(pos -> (pos.getX() >> 4) == cx && (pos.getZ() >> 4) == cz);
            }
        });
    }

    private void handleBlockUpdate(Dimension dimension, BlockPos pos, BlockState state) {
        addToQueue(() -> {
            for (Set<BlockPos> set: blocks.values()) {
                set.remove(pos);
            }
            checkBlock(state, pos);
        });
    }

    private void scan(Block block) {
        for (Pair<Dimension, WorldChunk> pair: ChunkController.instance.getLoadedChunks()) {
            scanChunkForBlock(pair.getFirst(), pair.getSecond(), block);
        }
    }

    private void scanChunkForBlock(Dimension dimension, WorldChunk chunk, Block block) {
        Set<BlockPos> set = blocks.get(block);
        int minY = dimension.getMinY();
        int xc = chunk.getPos().x << 4;
        int zc = chunk.getPos().z << 4;
        var pos = new BlockPos.Mutable();
        for (int x = 0; x < 16; x++) {
            pos.setX(xc | x);
            for (int z = 0; z < 16; z++) {
                pos.setZ(zc | z);
                int height = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE).get(x, z);
                for (int y = minY; y <= height; y++) {
                    pos.setY(y);
                    BlockState state = chunk.getBlockState(pos);
                    if (state.getBlock() == block) {
                        set.add(pos.toImmutable());
                    }
                }
            }
        }
    }

    private void checkBlock(BlockState state, BlockPos pos) {
        if (state.isAir()) {
            return;
        }

        Set<BlockPos> set = blocks.get(state.getBlock());
        if (set != null) {
            set.add(pos.toImmutable());
        }
    }

    private void addToQueue(Runnable runnable) {
        queue.add(runnable);
        synchronized (loopWaitEvent) {
            loopWaitEvent.notify();
        }
    }
}