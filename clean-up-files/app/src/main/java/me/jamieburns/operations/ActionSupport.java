package me.jamieburns.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.jamieburns.data.FileData;


public class ActionSupport {

    // private List<Action<FileData>> handleUniqueFile( FileData fileData ) {
    //     return List.of( new MoveAction<>( fileData ) );
    // }

    public Result actionListForUniqueFiles( Map<?, List<FileData>> groupedByMap ) {
        
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

    public Result actionListForDuplicateFiles( Map<?, List<FileData>> groupedByMap ) {

        var actionList = new ArrayList<Action<FileData>>();

        for( var e : groupedByMap.entrySet() ) {
            
            var list = e.getValue();

            actionList.add( new KeepAction<FileData>(list.get(0)));

            for( var index = 1; index < list.size(); index++ ) {
                actionList.add( new RemoveAction<FileData>( list.get(index)));
            }
        }

        return new Result( Map.of(), actionList);
    }

    public record Result( Map<?, List<FileData>> groupByMap, List<Action<FileData>> actionList ) {}
}


