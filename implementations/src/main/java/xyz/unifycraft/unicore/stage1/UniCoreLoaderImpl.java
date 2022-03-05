package xyz.unifycraft.unicore.stage1;

import xyz.unifycraft.unicore.stage0.UniCoreLoader;
import xyz.unifycraft.unicore.stage0.UniCoreSetup;

import java.io.File;
import java.lang.reflect.Method;

public class UniCoreLoaderImpl implements UniCoreLoader {
    public void initialize() {
        { // Download and load UniCore.
            String url = UniCoreSetup.getData().getUrl()
                    .replaceAll("\\{version}", UniCoreSetup.getData().getVersion())
                    .replaceAll("\\{gameversion}", UniCoreSetup.getGameVersion())
                    .replaceAll("\\{platform}", UniCoreSetup.getPlatform());
            File file = new File(UniCoreSetup.getVersionDir(), "UniCore-" + UniCoreSetup.getGameVersion() + "-" + UniCoreSetup.getPlatform() + "-" + UniCoreSetup.getData().getVersion() + ".jar");
            if (!file.exists()) UniCoreSetup.downloadFile(url, file.toPath());
            UniCoreSetup.addToClasspath(file.toPath());
        }

        { // Initialize UniCore through it's initialization class.
            try {
                Class<?> clz = Class.forName(UniCoreSetup.getData().getInitializer().getClassName());
                Method method = clz.getDeclaredMethod(UniCoreSetup.getData().getInitializer().getMethod());
                method.invoke(null);
            } catch (Exception e) {
                throw new IllegalStateException("Couldn't initialize UniCore!", e);
            }
        }
    }
}