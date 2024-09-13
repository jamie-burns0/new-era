package me.jamieburns;

import me.jamieburns.actions.KeepAction;
import me.jamieburns.actions.RemoveAction;
import me.jamieburns.operations.CleanApplicationRunner;
import me.jamieburns.operations.CleanApplicationRunner.RunnerArgs;

public class CleanApplication {

    public static void main(String[] args) {

        var actionList = CleanApplicationRunner.run(
                //new Args( "/mnt/c/Users/Jamie/OneDrive/Pictures-hp/jamie-iphone", "[Jj][Pp][Gg]$" ));
                new RunnerArgs( "/mnt/c/Users/Jamie/OneDrive/Pictures-hp", "[Jj][Pp][Gg]$" ));
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
}
