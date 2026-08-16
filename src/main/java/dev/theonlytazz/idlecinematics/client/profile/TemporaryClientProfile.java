package dev.theonlytazz.idlecinematics.client.profile;

import com.google.gson.Gson;
import dev.theonlytazz.idlecinematics.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/** Transaction for temporary vanilla option changes with crash-safe, per-field ownership. */
public final class TemporaryClientProfile {
    private static final Logger LOGGER = LoggerFactory.getLogger("Idle Cinematics");
    private static final Gson GSON = new Gson();
    private static final int SCHEMA_VERSION = 1;
    private static final Path RECOVERY_PATH = FMLPaths.CONFIGDIR.get().resolve("idlecinematics-runtime-recovery.json");
    private static boolean active;
    private static boolean recoveryPending;
    private static boolean warned;
    private static RecoveryRecord record;

    private TemporaryClientProfile() {}

    /** Called during mod construction; actual option repair waits until Minecraft has live options. */
    public static void recoverAtStartup() { recoveryPending = Files.isRegularFile(RECOVERY_PATH); }

    public static void repairIfNeeded(Minecraft minecraft) {
        if (!recoveryPending || minecraft.options == null) return;
        recoveryPending = false;
        try {
            RecoveryRecord stale = read();
            if (stale.schemaVersion != SCHEMA_VERSION) throw new IOException("unsupported recovery schema " + stale.schemaVersion);
            restoreOwned(minecraft, stale);
            Files.deleteIfExists(RECOVERY_PATH);
        } catch (IOException | RuntimeException exception) {
            warnOnce("Could not fully restore a stale cinematic client profile; continuing with current safe values", exception);
        }
    }

    public static void start(Minecraft minecraft) {
        repairIfNeeded(minecraft);
        if (active) { sync(minecraft); return; }
        Integer oldFps = ClientConfig.FPS_CAP_ENABLED.getAsBoolean() ? minecraft.options.framerateLimit().get() : null;
        Double oldVolume = ClientConfig.AUDIO_PROFILE_ENABLED.getAsBoolean()
                ? minecraft.options.getSoundSourceOptionInstance(SoundSource.MASTER).get() : null;
        Integer appliedFps = oldFps == null ? null : ClientConfig.FPS_CAP.getAsInt();
        Double appliedVolume = oldVolume == null ? null : ClientConfig.MASTER_VOLUME.getAsDouble();
        record = new RecoveryRecord(SCHEMA_VERSION, oldFps, appliedFps, oldVolume, appliedVolume);
        if (oldFps == null && oldVolume == null) { active = true; return; }
        try {
            write(record);
            apply(minecraft, record);
            active = true;
        } catch (IOException | RuntimeException exception) {
            warnOnce("Could not apply the temporary cinematic client profile; continuing without it", exception);
            record = null;
            active = false;
        }
    }

    public static void sync(Minecraft minecraft) {
        if (!active || record == null) return;
        Integer currentFps = minecraft.options.framerateLimit().get();
        Double currentVolume = minecraft.options.getSoundSourceOptionInstance(SoundSource.MASTER).get();
        Integer originalFps = record.originalFps;
        Double originalVolume = record.originalVolume;
        if (!ClientConfig.FPS_CAP_ENABLED.getAsBoolean() && record.appliedFps != null
                && Objects.equals(currentFps, record.appliedFps)) minecraft.options.framerateLimit().set(record.originalFps);
        if (!ClientConfig.AUDIO_PROFILE_ENABLED.getAsBoolean() && record.appliedVolume != null
                && approximately(currentVolume, record.appliedVolume)) minecraft.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(record.originalVolume);
        if (ClientConfig.FPS_CAP_ENABLED.getAsBoolean() && originalFps == null) originalFps = currentFps;
        if (ClientConfig.AUDIO_PROFILE_ENABLED.getAsBoolean() && originalVolume == null) originalVolume = currentVolume;
        RecoveryRecord next = new RecoveryRecord(SCHEMA_VERSION, originalFps,
                ClientConfig.FPS_CAP_ENABLED.getAsBoolean() ? ClientConfig.FPS_CAP.getAsInt() : null,
                originalVolume, ClientConfig.AUDIO_PROFILE_ENABLED.getAsBoolean() ? ClientConfig.MASTER_VOLUME.getAsDouble() : null);
        try {
            if (next.appliedFps == null && next.appliedVolume == null) Files.deleteIfExists(RECOVERY_PATH); else write(next);
            apply(minecraft, next); record = next;
        }
        catch (IOException | RuntimeException exception) { warnOnce("Could not synchronize the cinematic client profile", exception); }
    }

    public static void stop(Minecraft minecraft) {
        if (!active || record == null) return;
        try {
            restoreOwned(minecraft, record);
            Files.deleteIfExists(RECOVERY_PATH);
        } catch (IOException | RuntimeException exception) {
            warnOnce("Could not fully restore the cinematic client profile; startup recovery will retry", exception);
        } finally {
            active = false;
            record = null;
        }
    }

    private static void apply(Minecraft minecraft, RecoveryRecord value) {
        if (value.appliedFps != null) minecraft.options.framerateLimit().set(value.appliedFps);
        if (value.appliedVolume != null) minecraft.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(value.appliedVolume);
    }

    private static void restoreOwned(Minecraft minecraft, RecoveryRecord value) {
        if (value.originalFps != null && value.appliedFps != null
                && Objects.equals(minecraft.options.framerateLimit().get(), value.appliedFps)) {
            minecraft.options.framerateLimit().set(value.originalFps);
        }
        if (value.originalVolume != null && value.appliedVolume != null
                && approximately(minecraft.options.getSoundSourceOptionInstance(SoundSource.MASTER).get(), value.appliedVolume)) {
            minecraft.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(value.originalVolume);
        }
    }

    public static <T> T restoreIfOwned(T current, T applied, T original) {
        return Objects.equals(current, applied) ? original : current;
    }

    private static RecoveryRecord read() throws IOException {
        RecoveryRecord value = GSON.fromJson(Files.readString(RECOVERY_PATH), RecoveryRecord.class);
        if (value == null) throw new IOException("empty recovery record");
        return value;
    }

    private static void write(RecoveryRecord value) throws IOException {
        Files.createDirectories(RECOVERY_PATH.getParent());
        Path temporary = RECOVERY_PATH.resolveSibling(RECOVERY_PATH.getFileName() + ".tmp");
        Files.writeString(temporary, GSON.toJson(value));
        try { Files.move(temporary, RECOVERY_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
        catch (java.nio.file.AtomicMoveNotSupportedException exception) { Files.move(temporary, RECOVERY_PATH, StandardCopyOption.REPLACE_EXISTING); }
    }

    private static boolean approximately(double left, double right) { return Math.abs(left - right) < 1.0e-6; }
    private static void warnOnce(String message, Exception exception) {
        if (!warned) { warned = true; LOGGER.warn("{}: {}", message, exception.getMessage()); }
    }

    private record RecoveryRecord(int schemaVersion, Integer originalFps, Integer appliedFps,
                                  Double originalVolume, Double appliedVolume) {}
}
