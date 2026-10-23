package tuhljin.automagy.lib;

import cpw.mods.fml.common.FMLLog;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class VersionCheckerThread extends Thread {
    public VersionCheckerThread() {
        this.setName("Automagy Version Checker Thread");
        this.setDaemon(true);
        this.start();
    }

    @Override
    public void run() {
        try {
            URL url = new URL("http://everburningtorch.com/minecraft/automagy/checkVersion/?v=0.28.2");
            URLConnection con = url.openConnection();
            con.setUseCaches(false);
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String versionFound = br.readLine();
            if (versionFound != null && !Character.isDigit(versionFound.charAt(0))) {
                versionFound = null;
            }

            VersionChecker.latestVersion = versionFound;
            if (versionFound == null || versionFound.isEmpty()) {
                FMLLog.info("[Automagy] Version Checker: Unable to determine latest release version.");
            } else if (versionFound.equals("0.28.2")) {
                FMLLog.info("[Automagy] Version Checker: You are running the latest release version (" + versionFound + ").");
            } else {
                FMLLog.info("[Automagy] Version Checker: The latest release version is " + versionFound + ". You are running " + "0.28.2" + ".");
            }

            String line = null;

            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    VersionChecker.latestDesc.add(line);
                }
            }

            br.close();
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        VersionChecker.checkComplete = true;
    }
}
