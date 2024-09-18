package me.jamieburns.support;

import java.util.stream.Collectors;

public class CleanApplicationRunner {

    public static final void run( RunnerArgs args ) {

        var mark1 = System.currentTimeMillis();
        var fileDataList = FileDataSupport.collectFileData( args.onPath(), args.withFilenameFilter() );
        var mark2 = System.currentTimeMillis();

        System.out.println( "[CleanApplicationRunner]: found %s files in %s ms".formatted( fileDataList.size(), mark2 - mark1 ) );

        if( fileDataList.isEmpty() ) {
            return;
        }

        mark1 = System.currentTimeMillis();
        var actionList = ActionListBuilder.buildActionList( fileDataList );
        mark2 = System.currentTimeMillis();
        System.out.println( "[CleanApplicationRunner]: built %s actions in %s ms".formatted( actionList.size(), mark2 - mark1 ));

        actionList.stream()
                .collect(
                        Collectors.groupingBy( action -> action.getClass().getSimpleName(),
                        Collectors.counting()
                ))
                .forEach( (action, count) -> System.out.println( "%s %s".formatted( count, action )));
    }


    public record RunnerArgs( String onPath, String withFilenameFilter ) {}
}
