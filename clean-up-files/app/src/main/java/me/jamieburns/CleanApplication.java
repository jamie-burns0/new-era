package me.jamieburns;

import me.jamieburns.support.CleanApplicationRunner;
import me.jamieburns.support.CleanApplicationRunner.RunnerArgs;

public class CleanApplication {

    public static void main(String[] args) throws InterruptedException {

        //Thread.sleep(15000);
        CleanApplicationRunner.run(
                //new Args( "/mnt/c/Users/Jamie/OneDrive/Pictures-hp/jamie-iphone", "[Jj][Pp][Gg]$" ));
                new RunnerArgs( "/mnt/c/Users/Jamie/OneDrive/Pictures-hp", "[Jj][Pp][Gg]$" ));
                //new RunnerArgs( "/mnt/x/backup xperia xz", "[Jj][Pp][Gg]$" ));
                //new Args( "/mnt/c/Users/Jamie/AppData/Local/Temp/clean-files-test", "([Jj][Pp][Gg]|txt)$" ));
        //Thread.sleep(15000);
    }
}
