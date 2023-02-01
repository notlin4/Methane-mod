package me.wolfie.methane.mixin;

import me.wolfie.methane.Methane;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.s2c.play.LightData;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.BitSet;
import java.util.Iterator;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class LightingUpdateMixin implements PacketListener {

    @Shadow
    private ClientWorld world;
    @Shadow
    private MinecraftClient client;

    /**
     * @author AnOpenSauceDev
     * @reason Disable Function
     */
    @Overwrite
    private void readLightData(int x, int z, LightData data) {
        if(!Methane.ModActive){
            readLightDataVanilla(x,z,data);
        }
    }

    void readLightDataVanilla(int x, int z, LightData data){
        LightingProvider lightingProvider = this.world.getChunkManager().getLightingProvider();
        BitSet bitSet = data.getInitedSky();
        BitSet bitSet2 = data.getUninitedSky();
        Iterator<byte[]> iterator = data.getSkyNibbles().iterator();
        this.updateLighting(x, z, lightingProvider, LightType.SKY, bitSet, bitSet2, iterator, data.isNonEdge());
        BitSet bitSet3 = data.getInitedBlock();
        BitSet bitSet4 = data.getUninitedBlock();
        Iterator<byte[]> iterator2 = data.getBlockNibbles().iterator();
        this.updateLighting(x, z, lightingProvider, LightType.BLOCK, bitSet3, bitSet4, iterator2, data.isNonEdge());
        this.world.markChunkRenderability(x, z);
    }


    /**
     * @author
     * @reason
     */
    @Overwrite
    private void updateLighting(int chunkX, int chunkZ, LightingProvider provider, LightType type, BitSet inited, BitSet uninited, Iterator<byte[]> nibbles, boolean nonEdge) {
        if(!Methane.ModActive){
            updateLightingVanilla(chunkX,chunkZ,provider,type,inited,uninited,nibbles,nonEdge);
        }
    }

    public void updateLightingVanilla(int chunkX, int chunkZ, LightingProvider provider, LightType type, BitSet inited, BitSet uninited, Iterator<byte[]> nibbles, boolean nonEdge){
        for (int i = 0; i < provider.getHeight(); ++i) {
            int j = provider.getBottomY() + i;
            boolean bl = inited.get(i);
            boolean bl2 = uninited.get(i);
            if (!bl && !bl2) continue;
            provider.enqueueSectionData(type, ChunkSectionPos.from(chunkX, j, chunkZ), bl ? new ChunkNibbleArray((byte[])nibbles.next().clone()) : new ChunkNibbleArray(), nonEdge);
            this.world.scheduleBlockRenders(chunkX, j, chunkZ);
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void onLightUpdate(LightUpdateS2CPacket packet) { // who needs it anyway?
        if(!Methane.ModActive){
            onLightUpdateVanilla(packet);
        }
    }
    public void onLightUpdateVanilla(LightUpdateS2CPacket packet){
        // beginning of "bad" code
        PacketListener listener = this;
        ClientPlayNetworkHandler handler = (ClientPlayNetworkHandler) listener; // evil gross dark magic
        // end of "bad" code

        NetworkThreadUtils.forceMainThread(packet, handler , this.client);
        int i = packet.getChunkX();
        int j = packet.getChunkZ();
        LightData lightData = packet.getData();
        this.world.enqueueChunkUpdate(() -> this.readLightData(i, j, lightData));
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void updateChunk(int x, int z, LightData lightData) {
        if(Methane.ModActive){
        this.world.enqueueChunkUpdate(() -> {
            //this.readLightData(x, z, lightData);
            WorldChunk worldChunk = this.world.getChunkManager().getWorldChunk(x, z, false);
            if (worldChunk != null) {
                this.scheduleRenderChunk(worldChunk, x, z);
            }

        });
        }else {
            updateChunkVanilla(x,z,lightData);
        }
    }

    public void updateChunkVanilla(int x, int z, LightData lightData){
        this.world.enqueueChunkUpdate(() -> {
            this.readLightData(x, z, lightData);
            WorldChunk worldChunk = this.world.getChunkManager().getWorldChunk(x, z, false);
            if (worldChunk != null) {
                this.scheduleRenderChunk(worldChunk, x, z);
            }
        });
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void scheduleRenderChunk(WorldChunk chunk, int x, int z) {
        if(Methane.ModActive) {
            //  LightingProvider lightingProvider = this.world.getChunkManager().getLightingProvider();
            ChunkSection[] chunkSections = chunk.getSectionArray();
            ChunkPos chunkPos = chunk.getPos();
            //lightingProvider.setColumnEnabled(chunkPos, true);

            for (int i = 0; i < chunkSections.length; ++i) {
                ChunkSection chunkSection = chunkSections[i];
                int j = this.world.sectionIndexToCoord(i);
                //lightingProvider.setSectionStatus(ChunkSectionPos.from(chunkPos, j), chunkSection.isEmpty());
                this.world.scheduleBlockRenders(x, j, z);
            }

            this.world.markChunkRenderability(x, z);
        }else {
            scheduleRenderChunkVanilla(chunk,x,z);
        }
    }

    void scheduleRenderChunkVanilla(WorldChunk chunk, int x, int z){
        LightingProvider lightingProvider = this.world.getChunkManager().getLightingProvider();
        ChunkSection[] chunkSections = chunk.getSectionArray();
        ChunkPos chunkPos = chunk.getPos();
        lightingProvider.setColumnEnabled(chunkPos, true);
        for (int i = 0; i < chunkSections.length; ++i) {
            ChunkSection chunkSection = chunkSections[i];
            int j = this.world.sectionIndexToCoord(i);
            lightingProvider.setSectionStatus(ChunkSectionPos.from(chunkPos, j), chunkSection.isEmpty());
            this.world.scheduleBlockRenders(x, j, z);
        }
        this.world.markChunkRenderability(x, z);
    }




}
