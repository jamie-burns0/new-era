package me.jamieburns;

import me.jamieburns.operations.KeepAction;
import me.jamieburns.operations.RemoveAction;

public class CleanUpFilesMain {

    public static void main(String[] args) {

        var actionList = CleanUpFilesSupport.buildActionList(
                //new Args( "/mnt/c/Users/Jamie/OneDrive/Pictures-hp/jamie-iphone", "[Jj][Pp][Gg]$" ));
                new Args( "/mnt/c/Users/Jamie/OneDrive/Pictures-hp", "[Jj][Pp][Gg]$" ));
                //new Args( "/mnt/c/Users/Jamie/AppData/Local/Temp/clean-files-test", "([Jj][Pp][Gg]|txt)$" ));

        var moveActionCount = actionList.stream()
            .filter(action -> action instanceof KeepAction)
            .count();

        var removeActionCount = actionList.stream()
            .filter(action -> action instanceof RemoveAction)
            .count();

        System.out.println("Generated \n%s MoveAction\n%s RemoveAction".formatted(moveActionCount, removeActionCount));

        actionList.stream()
                //.filter( action -> action instanceof RemoveAction )
                .forEach( a -> System.out.println( "[%s], %s, (%s bytes), %s, [%s]".formatted(
                        a.getClass().getSimpleName(),
                        a.data().filename(),
                        a.data().sizeInBytes(),
                        a.data().path(),
                        a.data().hash()
                )));
    }

    public record Args( String path, String filenameFilter ) {}
}
