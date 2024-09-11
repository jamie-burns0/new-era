package me.jamieburns.operations;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.jamieburns.data.FileData;


public class ActionSupport {

    private ActionSupport() {}

    public static final List<Action<FileData>> actionListForZeroLengthFiles( List<FileData> fileDataList ) {

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return List.of();
        }

        return fileDataList.stream()
                .filter( fd -> fd.sizeInBytes() == 0 )
                .map( item -> new RemoveAction<FileData>( item ) )
                .collect( Collectors.toList() );
    }


    public static final List<Action<FileData>> actionListForUniqueFiles( List<FileData> fileDataList ) {

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return List.of();
        }

        return fileDataList.stream()
                .map( item -> new KeepAction<>( item ) )
                .collect( Collectors.toList() );
    }


    public static final List<Action<FileData>> actionListForDuplicateFiles( Map<?, List<FileData>> groupedItemsMap ) {

        if( groupedItemsMap == null || groupedItemsMap.isEmpty() ) {
            return List.of();
        }

        var actionList = new LinkedList<Action<FileData>>();

        for( var list : groupedItemsMap.values() ) {

            actionList.add( new KeepAction<>(list.get(0)));

            for( var index = 1; index < list.size(); index++ ) {
                actionList.add( new RemoveAction<FileData>( list.get(index)));
            }
        }

        return actionList;
    }


    public static final List<Action<FileData>> actionListForUniqueFilesWithDuplicateFilename( List<FileData> fileDataList ) {

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return List.of();
        }

        var groupByFilenameMap = GroupFilesSupport.groupFilesByFilename( fileDataList );

        var actionList = new LinkedList<Action<FileData>>();

        for( var list : groupByFilenameMap.values() ) {

            actionList.add( new KeepAction<FileData>( list.get(0) ) );

            for( var index = 1; index < list.size(); index++ ) {
                actionList.add( new KeepWithRenameAction<FileData>( list.get(index) ) );
            }
        }

        return actionList;
    }


    public static final List<Action<FileData>> actionListForKeepActionWithDuplicateFilename( List<Action<FileData>> actionList ) {

        if( actionList == null || actionList.size() == 0 ) {
            return List.of();
        }

        var groupedByList = actionList.stream()
                .collect( Collectors.groupingBy( a -> a.data().filename() ) );

        List<Action<FileData>> newActionList = new LinkedList<>();

        for( var list : groupedByList.values() ) {

            newActionList.add( new KeepAction<>(list.get(0).data()));

            for( var index = 1; index < list.size(); index++ ) {
                newActionList.add( new KeepWithRenameAction<FileData>( list.get(index).data()));
            }
        }

        return newActionList;
    }
}