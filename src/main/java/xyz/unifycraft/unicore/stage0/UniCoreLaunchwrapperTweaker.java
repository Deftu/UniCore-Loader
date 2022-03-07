package xyz.unifycraft.unicore.stage0;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
//#if FORGE == 1
import net.minecraftforge.common.ForgeVersion;
//#else
//$$ import net.fabricmc.loader.impl.FabricLoaderImpl;
//#endif

import java.io.File;
import java.util.List;

//#if FORGE == 1
public class UniCoreLaunchwrapperTweaker implements ITweaker {
    private static UniCoreLoaderBase uniCoreLoader;

    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        uniCoreLoader = new UniCoreLoader(ForgeVersion.mcVersion, "forge", this);
    }

    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        uniCoreLoader.initialize();
    }

    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

    public String[] getLaunchArguments() {
        return new String[0];
    }

    public static UniCoreLoaderBase getUniCoreLoader() {
        return uniCoreLoader;
    }
}
//#else
//$$ public class UniCoreSetupTweaker implements PreLaunchEntrypoint {
//$$     private static UniCoreLoaderBase uniCoreLoader;
//$$
//$$     public void onPreLaunch() {
//$$         uniCoreLoader = new UniCoreLoader();
//$$         uniCoreLoader.initialize(FabricLoaderImpl.INSTANCE.getGameProvider().getNormalizedGameVersion(), "fabric", null);
//$$     }
//$$
//$$     public static UniCoreLoaderBase getUniCoreLoader() {
//$$        return uniCoreLoader;
//$$    }
//$$ }
//#endif