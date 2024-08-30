package me.jamieburns.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.jamieburns.data.FileData;


public class ActionSupport {

    private ActionSupport() {}

    // private List<Action<FileData>> handleUniqueFile( FileData fileData ) {
    //     return List.of( new MoveAction<>( fileData ) );
    // }

    public static final Result actionListForUniqueFiles( Map<?, List<FileData>> groupedByMap ) {

        if( groupedByMap == null || groupedByMap.isEmpty() ) {
            return new Result( Map.of(), List.of() );
        }

        var actionList = new ArrayList<Action<FileData>>();

        groupedByMap.entrySet().removeIf(entry -> {
            if (entry.getValue().size() == 1) {
                actionList.add(new KeepAction<>(entry.getValue().get(0)));
                return true; // Remove this entry from the map
            }
            return false; // Keep this entry in the map
        });

        return new Result(groupedByMap, actionList);
    }

    public static final Result actionListForDuplicateFiles( Map<?, List<FileData>> groupedByMap ) {

        if( groupedByMap == null || groupedByMap.isEmpty() ) {
            return new Result( Map.of(), List.of() );
        }

        var actionList = new ArrayList<Action<FileData>>();

        groupedByMap.entrySet().removeIf( e -> {
            if(e.getValue().size() == 1) {
                return false;
            }
            var list = e.getValue();
            actionList.add( new KeepAction<FileData>(list.get(0)));
            for( var index = 1; index < list.size(); index++ ) {
                actionList.add( new RemoveAction<FileData>( list.get(index)));
            }
            return true;
        });

        return new Result( groupedByMap, actionList);
    }

    public record Result( Map<?, List<FileData>> groupByMap, List<Action<FileData>> actionList ) {}
}