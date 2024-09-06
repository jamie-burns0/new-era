package me.jamieburns.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.jamieburns.data.FileData;


public class ActionSupport {

    private ActionSupport() {}

    public static final List<Action<FileData>> actionListForZeroLengthFiles( List<FileData> zeroLengthItemsList ) {

        if( zeroLengthItemsList == null || zeroLengthItemsList.isEmpty() ) {
            return List.of();
        }

        return zeroLengthItemsList.stream()
                .filter( fd -> fd.sizeInBytes() == 0 )
                .map( item -> new RemoveAction<FileData>( item ) )
                .collect( Collectors.toList() );
    }


    public static final List<Action<FileData>> actionListForUniqueFiles( List<FileData> uniqueItemsList ) {

        if( uniqueItemsList == null || uniqueItemsList.isEmpty() ) {
            return List.of();
        }

        return uniqueItemsList.stream()
                .map( item -> new KeepAction<>( item ) )
                .collect( Collectors.toList() );
    }

    public static final List<Action<FileData>> actionListForDuplicateFiles( Map<?, List<FileData>> nonUniqueItemMap ) {

        if( nonUniqueItemMap == null || nonUniqueItemMap.isEmpty() ) {
            return List.of();
        }

        var actionList = new ArrayList<Action<FileData>>();

        for( var list : nonUniqueItemMap.values() ) {

            actionList.add( new KeepAction<>(list.get(0)));

            for( var index = 1; index < list.size(); index++ ) {
                actionList.add( new RemoveAction<FileData>( list.get(index)));
            }
        }

        return actionList;
    }


    public static final List<Action<FileData>> actionListFromGroupedActionList( Map<?, List<Action<FileData>>> groupedActionMap ) {

            if( groupedActionMap == null || groupedActionMap.isEmpty() ) {
                return List.of();
            }

            return groupedActionMap.values().stream()
                    .flatMap( List::stream )
                    .collect( Collectors.toList() );

    }

    public static final List<Action<FileData>> actionListForKeepActionWithDuplicateFilename( Map<?, List<Action<FileData>>> actionListGroupedByFilenameMap ) {

        if( actionListGroupedByFilenameMap == null || actionListGroupedByFilenameMap.size() == 0 ) {
            return List.of();
        }

        var actionList = new ArrayList<Action<FileData>>();

        for( var list : actionListGroupedByFilenameMap.values() ) {

            actionList.add( new KeepAction<>(list.get(0).data()));

            for( var index = 1; index < list.size(); index++ ) {
                actionList.add( new KeepWithRenameAction<FileData>( list.get(index).data()));
            }
        }

        return actionList;
    }
}