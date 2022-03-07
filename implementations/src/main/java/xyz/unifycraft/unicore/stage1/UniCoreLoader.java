package xyz.unifycraft.unicore.stage1;

import net.minecraft.launchwrapper.Launch;
import xyz.unifycraft.unicore.stage0.CopyInputStream;
import xyz.unifycraft.unicore.stage0.UniCoreData;
import xyz.unifycraft.unicore.stage0.UniCoreLoaderBase;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.util.Objects;

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
        { // Download and load UniCore.
            String url = data.getUrl()
                    .replaceAll("\\{version}", data.getVersion())
                    .replaceAll("\\{gameversion}", gameVersion)
                    .replaceAll("\\{platform}", gamePlatform);
            File file = new File(versionDir, "UniCore-" + gameVersion + "-" + gamePlatform + "-" + data.getVersion() + ".jar");
            File localFile = new File(versionDir, "UniCore-" + gameVersion + "-" + gamePlatform + "-" + localData.getVersion() + ".jar");
            if (!Objects.equals(localData.getLoaderVersion(), data.getLoaderVersion()) || !localFile.exists()) {
                HttpURLConnection connection = null;
                try (FileOutputStream output = new FileOutputStream(localFile)) {
                    connection = createConnection(url);
                    try (CopyInputStream stream = new CopyInputStream(connection.getInputStream())) {
                        if (!Objects.equals(fetchChecksum(stream.createCopy()), fetchChecksum(localFile.toPath()))) {
                            localFile.delete();
                            byte[] buffer = new byte[2048];
                            int read;
                            while ((read = stream.read(buffer)) > 0) output.write(buffer, 0, read);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) connection.disconnect();
                }
            }
            addToClasspath(Launch.classLoader, file.toPath());
        }

        { // Initialize UniCore through it's initialization class.
            try {
                Class<?> clz = Class.forName(data.getInitializer().getClassName());
                Method method = clz.getDeclaredMethod(data.getInitializer().getMethod());
                method.invoke(null);
            } catch (Exception e) {
                throw new IllegalStateException("Couldn't initialize UniCore!", e);
            }
        }
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