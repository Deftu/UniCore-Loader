package xyz.unifycraft.unicore.stage1;

import xyz.unifycraft.unicore.stage0.UniCoreLoader;
import xyz.unifycraft.unicore.stage0.UniCoreSetup;

import java.io.File;
import java.lang.reflect.Method;

public class UniCoreLoaderImpl implements UniCoreLoader {
    public void initialize() {
        { // Download and load UniCore.
            String url = UniCoreSetup.getVersion().getLoaderUrl()
                    .replaceAll("\\{version}", UniCoreSetup.getVersion().getLoaderVersion())
                    .replaceAll("\\{gameversion}", UniCoreSetup.getGameVersion())
                    .replaceAll("\\{platform}", UniCoreSetup.getPlatform());
            File file = new File(UniCoreSetup.getVersionDir(), "UniCore-" + UniCoreSetup.getGameVersion() + "-" + UniCoreSetup.getPlatform() + "-" + UniCoreSetup.getVersion().getVersion() + ".jar");
            if (!file.exists()) UniCoreSetup.downloadFile(url, file.toPath());
            UniCoreSetup.addToClasspath(file.toPath());
        }

        { // Initialize UniCore through it's initialization class.
            try {
                Class<?> clz = Class.forName(UniCoreSetup.getVersion().getInitializer().getClassName());
                Method method = clz.getDeclaredMethod(UniCoreSetup.getVersion().getInitializer().getMethod());
                method.invoke(null);
            } catch (Exception e) {
                throw new IllegalStateException("Couldn't initialize UniCore!", e);
            }
        }
    }
}