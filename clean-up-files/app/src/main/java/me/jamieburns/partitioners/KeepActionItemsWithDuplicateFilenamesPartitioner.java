package me.jamieburns.partitioners;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.jamieburns.actions.Action;
import me.jamieburns.actions.KeepAction;
import me.jamieburns.data.FileData;
import me.jamieburns.support.Timer;

public final class KeepActionItemsWithDuplicateFilenamesPartitioner implements Partitioner {

    public static final PartitionResult<Action<FileData>> partition( List<Action<FileData>> actionList ) {

        if( actionList == null || actionList.isEmpty() ) {
            return new PartitionResult<>( List.of(), List.of() );
        }

        List<Action<FileData>> negativeList = new LinkedList<>();
        var timer = new Timer<>(KeepActionItemsWithDuplicateFilenamesPartitioner.class).start();

        var partitionMap = actionList.stream()
                .collect( Collectors.partitioningBy( a -> a instanceof KeepAction ) );
        // positive = all KeepAction items
        // negative = all other items

        negativeList.addAll( partitionMap.get( Boolean.FALSE ) );

        var partitionMap2 = partitionMap.get(Boolean.TRUE).stream()
                .collect( Collectors.groupingBy( action -> action.data().filename() ) )
                .values().stream()
                .collect( Collectors.partitioningBy( list -> list.size() > 1 ))
                .entrySet().stream()
                .collect( Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .flatMap( List::stream )
                                .collect( Collectors.toList() )
                ));

        negativeList.addAll( partitionMap2.get( Boolean.FALSE ) );
        timer.stop();
        return new PartitionResult<>( partitionMap2.get( Boolean.TRUE ), negativeList );
        // positive = all KeepAction items with duplicate filenames
        // negative = all other KeepAction items
    }

}
