package xyz.unifycraft.unicore.stage0;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.util.List;

//#if FORGE == 1
public class UniCoreSetupTweaker implements ITweaker {
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
    }

    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        UniCoreSetup.initialize();
    }

    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

    public String[] getLaunchArguments() {
        return new String[0];
    }
}
//#else
//$$ public class UniCoreSetupTweaker implements PreLaunchEntrypoint {
//$$     public void onPreLaunch() {
//$$         UniCoreSetup.initialize();
//$$     }
//$$ }
//#endif