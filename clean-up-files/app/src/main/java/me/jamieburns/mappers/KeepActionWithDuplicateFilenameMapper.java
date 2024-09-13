package me.jamieburns.mappers;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import me.jamieburns.actions.Action;
import me.jamieburns.actions.KeepAction;
import me.jamieburns.actions.KeepWithRenameAction;
import me.jamieburns.data.FileData;

public final class KeepActionWithDuplicateFilenameMapper implements Mapper {

    public static final List<Action<FileData>> toActionList( List<Action<FileData>> actionList ) {

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
