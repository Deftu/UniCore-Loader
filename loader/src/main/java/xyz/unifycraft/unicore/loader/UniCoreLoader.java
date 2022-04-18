package xyz.unifycraft.unicore.loader;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.Mod;
import xyz.deftu.fd.FileDownloader;
import xyz.unifycraft.unicore.launchwrapper.UniCoreData;
import xyz.unifycraft.unicore.launchwrapper.UniCoreLoadException;
import xyz.unifycraft.unicore.launchwrapper.UniCoreLoaderBase;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Objects;

public class UniCoreLoader extends UniCoreLoaderBase {
    private File versionDir;
    private String gameVersion;
    private String gamePlatform;
    private UniCoreData data;
    private UniCoreData localData;

    public UniCoreLoader() {
        super("loader");
    }

    public void initialize() {
        String url = data.getUrl().replaceAll("\\{version}", data.getVersion()).replaceAll("\\{gameversion}", gameVersion).replaceAll("\\{platform}", gamePlatform);
        File file = new File(versionDir, "unicore-" + gameVersion + "-" + gamePlatform + "-" + data.getVersion() + ".jar");
        File localFile = new File(versionDir, "unicore-" + gameVersion + "-" + gamePlatform + "-" + localData.getVersion() + ".jar");
        if (!Objects.equals(localData.getVersion(), data.getVersion()) || !localFile.exists()) {
            try {
                FileDownloader fileDownloader = fileDownloaderFactory.create(downloadsDir, localFile);
                fileDownloader.download(url);
                fileDownloader.validate();
                fileDownloader.complete(file);
            } catch (Exception e) {
                throw new UniCoreLoadException("Failed to download UniCore.", e);
            }
        }
        addToClasspath(Launch.classLoader, file.toPath());
        //#if FORGE == 1
        //Launch.classLoader.addClassLoaderExclusion("xyz.unifycraft.unicore.");
        //#endif

        // Load and initialize UniCore.
        try {
            Class<?> clz = Class.forName(data.getInitializer().getClassName(), true, Launch.classLoader);
            useInitializerClass(clz);
        } catch (Exception e) {
            throw new UniCoreLoadException("Failed to load UniCore.", e);
        }
    }

    public void withProperties(File versionDir, String gameVersion, String gamePlatform) {
        this.versionDir = versionDir;
        this.gameVersion = gameVersion;
        this.gamePlatform = gamePlatform;
    }

    public void withData(UniCoreData data, UniCoreData localData) {
        this.data = data;
        this.localData = localData;
    }

    private void useInitializerClass(Class<?> clz) {
        try {
            Method method = clz.getDeclaredMethod(data.getInitializer().getMethod());
            method.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}