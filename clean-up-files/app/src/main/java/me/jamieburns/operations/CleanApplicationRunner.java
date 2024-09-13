package me.jamieburns.operations;

import java.util.List;

import me.jamieburns.actions.Action;
import me.jamieburns.data.FileData;

public class CleanApplicationRunner {

    public static final List<Action<FileData>> run( RunnerArgs args ) {

        var fileDataList = FilesSupport.buildFileList( args.onPath(), args.withFilenameFilter() );

        if( fileDataList.isEmpty() ) {
            return List.of();
        }

        System.out.println( "[CleanApplicationRunner]: found %s files".formatted( fileDataList.size() ) );

        return ActionListBuilder.buildActionList( fileDataList );
    }


    public record RunnerArgs( String onPath, String withFilenameFilter ) {}
}
