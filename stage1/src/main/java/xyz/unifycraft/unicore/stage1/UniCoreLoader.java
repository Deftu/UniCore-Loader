package xyz.unifycraft.unicore.stage1;

import xyz.unifycraft.unicore.stage0.UniCoreData;
import xyz.unifycraft.unicore.stage0.UniCoreLoaderBase;

import java.io.File;

public class UniCoreLoader extends UniCoreLoaderBase {
    private File dataDir;
    private File versionDir;
    private String gameVersion;
    private String gamePlatform;
    private UniCoreData data;
    private UniCoreData localData;

    public UniCoreLoader() {
        super("stage1");
    }

    public void initialize() {
    }

    protected void withProperties(File dataDir, File versionDir, String gameVersion, String gamePlatform) {
        this.dataDir = dataDir;
        this.versionDir = versionDir;
        this.gameVersion = gameVersion;
        this.gamePlatform = gamePlatform;
    }

    protected void withData(UniCoreData data, UniCoreData localData) {
        this.data = data;
        this.localData = localData;
    }
}