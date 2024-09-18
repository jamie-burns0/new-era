package me.jamieburns.mappers;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.jamieburns.actions.Action;
import me.jamieburns.actions.KeepAction;
import me.jamieburns.actions.RemoveAction;
import me.jamieburns.data.FileData;
import me.jamieburns.support.Timer;

public final class DuplicateFilesMapper implements Mapper {

    public static final List<Action<FileData>> toActionList( Map<?, List<FileData>> groupedItemsMap ) {

        if( groupedItemsMap == null || groupedItemsMap.isEmpty() ) {
            return List.of();
        }

        var actionList = new LinkedList<Action<FileData>>();
        var timer = new Timer<>(DuplicateFilesMapper.class).start();

        for( var list : groupedItemsMap.values() ) {

            actionList.add( new KeepAction<>(list.get(0)));

            for( var index = 1; index < list.size(); index++ ) {
                actionList.add( new RemoveAction<FileData>( list.get(index)));
            }
        }
        timer.stop();
        return actionList;
    }

}
