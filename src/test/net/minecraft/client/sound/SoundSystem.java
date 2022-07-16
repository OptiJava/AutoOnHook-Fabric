/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.sound;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.client.sound.AudioStream;
import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundEngine;
import net.minecraft.client.sound.SoundExecutor;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundInstanceListener;
import net.minecraft.client.sound.SoundListener;
import net.minecraft.client.sound.SoundLoader;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.Source;
import net.minecraft.client.sound.StaticSound;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SoundSystem {
    private static final Marker MARKER = MarkerManager.getMarker("SOUNDS");
    private static final Logger LOGGER = LogManager.getLogger();
    private static final float field_33021 = 0.5f;
    private static final float field_33022 = 2.0f;
    private static final float field_33023 = 0.0f;
    private static final float field_33024 = 1.0f;
    private static final int field_33025 = 20;
    private static final Set<Identifier> UNKNOWN_SOUNDS = Sets.newHashSet();
    public static final String FOR_THE_DEBUG = "FOR THE DEBUG!";
    private final SoundManager loader;
    private final GameOptions settings;
    private boolean started;
    private final SoundEngine soundEngine = new SoundEngine();
    private final SoundListener listener = this.soundEngine.getListener();
    private final SoundLoader soundLoader;
    private final SoundExecutor taskQueue = new SoundExecutor();
    private final Channel channel = new Channel(this.soundEngine, this.taskQueue);
    private int ticks;
    private final Map<SoundInstance, Channel.SourceManager> sources = Maps.newHashMap();
    private final Multimap<SoundCategory, SoundInstance> sounds = HashMultimap.create();
    private final List<TickableSoundInstance> tickingSounds = Lists.newArrayList();
    private final Map<SoundInstance, Integer> startTicks = Maps.newHashMap();
    private final Map<SoundInstance, Integer> soundEndTicks = Maps.newHashMap();
    private final List<SoundInstanceListener> listeners = Lists.newArrayList();
    private final List<TickableSoundInstance> soundsToPlayNextTick = Lists.newArrayList();
    private final List<Sound> preloadedSounds = Lists.newArrayList();

    public SoundSystem(SoundManager loader, GameOptions settings, ResourceManager resourceManager) {
        this.loader = loader;
        this.settings = settings;
        this.soundLoader = new SoundLoader(resourceManager);
    }

    public void reloadSounds() {
        UNKNOWN_SOUNDS.clear();
        for (SoundEvent soundEvent : Registry.SOUND_EVENT) {
            Identifier identifier = soundEvent.getId();
            if (this.loader.get(identifier) != null) continue;
            LOGGER.warn("Missing sound for event: {}", (Object)Registry.SOUND_EVENT.getId(soundEvent));
            UNKNOWN_SOUNDS.add(identifier);
        }
        this.stop();
        this.start();
    }

    private synchronized void start() {
        if (this.started) {
            return;
        }
        try {
            this.soundEngine.init();
            this.listener.init();
            this.listener.setVolume(this.settings.getSoundVolume(SoundCategory.MASTER));
            this.soundLoader.loadStatic(this.preloadedSounds).thenRun(this.preloadedSounds::clear);
            this.started = true;
            LOGGER.info(MARKER, "Sound engine started");
        }
        catch (RuntimeException runtimeException) {
            LOGGER.error(MARKER, "Error starting SoundSystem. Turning off sounds & music", (Throwable)runtimeException);
        }
    }

    private float getSoundVolume(@Nullable SoundCategory category) {
        if (category == null || category == SoundCategory.MASTER) {
            return 1.0f;
        }
        return this.settings.getSoundVolume(category);
    }

    public void updateSoundVolume(SoundCategory category, float volume) {
        if (!this.started) {
            return;
        }
        if (category == SoundCategory.MASTER) {
            this.listener.setVolume(volume);
            return;
        }
        this.sources.forEach((source2, sourceManager) -> {
            float f = this.getAdjustedVolume((SoundInstance)source2);
            sourceManager.run(source -> {
                if (f <= 0.0f) {
                    source.stop();
                } else {
                    source.setVolume(f);
                }
            });
        });
    }

    public void stop() {
        if (this.started) {
            this.stopAll();
            this.soundLoader.close();
            this.soundEngine.close();
            this.started = false;
        }
    }

    public void stop(SoundInstance sound) {
        Channel.SourceManager sourceManager;
        if (this.started && (sourceManager = this.sources.get(sound)) != null) {
            sourceManager.run(Source::stop);
        }
    }

    public void stopAll() {
        if (this.started) {
            this.taskQueue.restart();
            this.sources.values().forEach(source -> source.run(Source::stop));
            this.sources.clear();
            this.channel.close();
            this.startTicks.clear();
            this.tickingSounds.clear();
            this.sounds.clear();
            this.soundEndTicks.clear();
            this.soundsToPlayNextTick.clear();
        }
    }

    public void registerListener(SoundInstanceListener listener) {
        this.listeners.add(listener);
    }

    public void unregisterListener(SoundInstanceListener listener) {
        this.listeners.remove(listener);
    }

    public void tick(boolean bl) {
        if (!bl) {
            this.tick();
        }
        this.channel.tick();
    }

    private void tick() {
        ++this.ticks;
        this.soundsToPlayNextTick.stream().filter(SoundInstance::canPlay).forEach(this::play);
        this.soundsToPlayNextTick.clear();
        for (TickableSoundInstance tickableSoundInstance : this.tickingSounds) {
            if (!tickableSoundInstance.canPlay()) {
                this.stop(tickableSoundInstance);
            }
            tickableSoundInstance.tick();
            if (tickableSoundInstance.isDone()) {
                this.stop(tickableSoundInstance);
                continue;
            }
            float f = this.getAdjustedVolume(tickableSoundInstance);
            float g = this.getAdjustedPitch(tickableSoundInstance);
            Vec3d vec3d = new Vec3d(tickableSoundInstance.getX(), tickableSoundInstance.getY(), tickableSoundInstance.getZ());
            Channel.SourceManager sourceManager = this.sources.get(tickableSoundInstance);
            if (sourceManager == null) continue;
            sourceManager.run(source -> {
                source.setVolume(f);
                source.setPitch(g);
                source.setPosition(vec3d);
            });
        }
        Iterator<Map.Entry<SoundInstance, Channel.SourceManager>> iterator = this.sources.entrySet().iterator();
        while (iterator.hasNext()) {
            int sourceManager;
            Map.Entry<SoundInstance, Channel.SourceManager> entry = iterator.next();
            Channel.SourceManager f = entry.getValue();
            SoundInstance g = entry.getKey();
            float vec3d = this.settings.getSoundVolume(g.getCategory());
            if (vec3d <= 0.0f) {
                f.run(Source::stop);
                iterator.remove();
                continue;
            }
            if (!f.isStopped() || (sourceManager = this.soundEndTicks.get(g).intValue()) > this.ticks) continue;
            if (SoundSystem.isRepeatDelayed(g)) {
                this.startTicks.put(g, this.ticks + g.getRepeatDelay());
            }
            iterator.remove();
            LOGGER.debug(MARKER, "Removed channel {} because it's not playing anymore", (Object)f);
            this.soundEndTicks.remove(g);
            try {
                this.sounds.remove((Object)g.getCategory(), g);
            }
            catch (RuntimeException runtimeException) {
                // empty catch block
            }
            if (!(g instanceof TickableSoundInstance)) continue;
            this.tickingSounds.remove(g);
        }
        Iterator<Map.Entry<SoundInstance, Integer>> iterator2 = this.startTicks.entrySet().iterator();
        while (iterator2.hasNext()) {
            Map.Entry<SoundInstance, Integer> f = iterator2.next();
            if (this.ticks < f.getValue()) continue;
            SoundInstance g = f.getKey();
            if (g instanceof TickableSoundInstance) {
                ((TickableSoundInstance)g).tick();
            }
            this.play(g);
            iterator2.remove();
        }
    }

    private static boolean canRepeatInstantly(SoundInstance sound) {
        return sound.getRepeatDelay() > 0;
    }

    private static boolean isRepeatDelayed(SoundInstance sound) {
        return sound.isRepeatable() && SoundSystem.canRepeatInstantly(sound);
    }

    private static boolean shouldRepeatInstantly(SoundInstance sound) {
        return sound.isRepeatable() && !SoundSystem.canRepeatInstantly(sound);
    }

    public boolean isPlaying(SoundInstance sound) {
        if (!this.started) {
            return false;
        }
        if (this.soundEndTicks.containsKey(sound) && this.soundEndTicks.get(sound) <= this.ticks) {
            return true;
        }
        return this.sources.containsKey(sound);
    }

    public void play(SoundInstance sound) {
        boolean bl2;
        if (!this.started) {
            return;
        }
        if (!sound.canPlay()) {
            return;
        }
        WeightedSoundSet weightedSoundSet = sound.getSoundSet(this.loader);
        Identifier identifier = sound.getId();
        if (weightedSoundSet == null) {
            if (UNKNOWN_SOUNDS.add(identifier)) {
                LOGGER.warn(MARKER, "Unable to play unknown soundEvent: {}", (Object)identifier);
            }
            return;
        }
        Sound sound2 = sound.getSound();
        if (sound2 == SoundManager.MISSING_SOUND) {
            if (UNKNOWN_SOUNDS.add(identifier)) {
                LOGGER.warn(MARKER, "Unable to play empty soundEvent: {}", (Object)identifier);
            }
            return;
        }
        float f = sound.getVolume();
        float g = Math.max(f, 1.0f) * (float)sound2.getAttenuation();
        SoundCategory soundCategory = sound.getCategory();
        float h = this.getAdjustedVolume(sound);
        float i = this.getAdjustedPitch(sound);
        SoundInstance.AttenuationType attenuationType = sound.getAttenuationType();
        boolean bl = sound.isRelative();
        if (h == 0.0f && !sound.shouldAlwaysPlay()) {
            LOGGER.debug(MARKER, "Skipped playing sound {}, volume was zero.", (Object)sound2.getIdentifier());
            return;
        }
        Vec3d vec3d = new Vec3d(sound.getX(), sound.getY(), sound.getZ());
        if (!this.listeners.isEmpty()) {
            boolean bl3 = bl2 = bl || attenuationType == SoundInstance.AttenuationType.NONE || this.listener.getPos().squaredDistanceTo(vec3d) < (double)(g * g);
            if (bl2) {
                for (SoundInstanceListener soundInstanceListener : this.listeners) {
                    soundInstanceListener.onSoundPlayed(sound, weightedSoundSet);
                }
            } else {
                LOGGER.debug(MARKER, "Did not notify listeners of soundEvent: {}, it is too far away to hear", (Object)identifier);
            }
        }
        if (this.listener.getVolume() <= 0.0f) {
            LOGGER.debug(MARKER, "Skipped playing soundEvent: {}, master volume was zero", (Object)identifier);
            return;
        }
        bl2 = SoundSystem.shouldRepeatInstantly(sound);
        boolean bl3 = sound2.isStreamed();
        CompletableFuture<Channel.SourceManager> completableFuture = this.channel.createSource(sound2.isStreamed() ? SoundEngine.RunMode.STREAMING : SoundEngine.RunMode.STATIC);
        Channel.SourceManager sourceManager = completableFuture.join();
        if (sourceManager == null) {
            LOGGER.warn("Failed to create new sound handle");
            return;
        }
        LOGGER.debug(MARKER, "Playing sound {} for event {}", (Object)sound2.getIdentifier(), (Object)identifier);
        this.soundEndTicks.put(sound, this.ticks + 20);
        this.sources.put(sound, sourceManager);
        this.sounds.put(soundCategory, sound);
        sourceManager.run(source -> {
            source.setPitch(i);
            source.setVolume(h);
            if (attenuationType == SoundInstance.AttenuationType.LINEAR) {
                source.setAttenuation(g);
            } else {
                source.disableAttenuation();
            }
            source.setLooping(bl2 && !bl3);
            source.setPosition(vec3d);
            source.setRelative(bl);
        });
        if (!bl3) {
            this.soundLoader.loadStatic(sound2.getLocation()).thenAccept(staticSound -> sourceManager.run(source -> {
                source.setBuffer((StaticSound)staticSound);
                source.play();
            }));
        } else {
            this.soundLoader.loadStreamed(sound2.getLocation(), bl2).thenAccept(audioStream -> sourceManager.run(source -> {
                source.setStream((AudioStream)audioStream);
                source.play();
            }));
        }
        if (sound instanceof TickableSoundInstance) {
            this.tickingSounds.add((TickableSoundInstance)sound);
        }
    }

    public void playNextTick(TickableSoundInstance sound) {
        this.soundsToPlayNextTick.add(sound);
    }

    public void addPreloadedSound(Sound sound) {
        this.preloadedSounds.add(sound);
    }

    private float getAdjustedPitch(SoundInstance sound) {
        return MathHelper.clamp(sound.getPitch(), 0.5f, 2.0f);
    }

    private float getAdjustedVolume(SoundInstance sound) {
        return MathHelper.clamp(sound.getVolume() * this.getSoundVolume(sound.getCategory()), 0.0f, 1.0f);
    }

    public void pauseAll() {
        if (this.started) {
            this.channel.execute(stream -> stream.forEach(Source::pause));
        }
    }

    public void resumeAll() {
        if (this.started) {
            this.channel.execute(stream -> stream.forEach(Source::resume));
        }
    }

    public void play(SoundInstance sound, int delay) {
        this.startTicks.put(sound, this.ticks + delay);
    }

    public void updateListenerPosition(Camera camera) {
        if (!this.started || !camera.isReady()) {
            return;
        }
        Vec3d vec3d = camera.getPos();
        Vec3f vec3f = camera.getHorizontalPlane();
        Vec3f vec3f2 = camera.getVerticalPlane();
        this.taskQueue.execute(() -> {
            this.listener.setPosition(vec3d);
            this.listener.setOrientation(vec3f, vec3f2);
        });
    }

    public void stopSounds(@Nullable Identifier id, @Nullable SoundCategory category) {
        if (category != null) {
            for (SoundInstance soundInstance : this.sounds.get(category)) {
                if (id != null && !soundInstance.getId().equals(id)) continue;
                this.stop(soundInstance);
            }
        } else if (id == null) {
            this.stopAll();
        } else {
            for (SoundInstance soundInstance : this.sources.keySet()) {
                if (!soundInstance.getId().equals(id)) continue;
                this.stop(soundInstance);
            }
        }
    }

    public String getDebugString() {
        return this.soundEngine.getDebugString();
    }
}

