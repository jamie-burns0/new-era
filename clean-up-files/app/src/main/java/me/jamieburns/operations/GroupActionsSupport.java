package me.jamieburns.operations;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.jamieburns.data.FileData;

public class GroupActionsSupport {

    public static final Map<Boolean, Map<String, List<Action<FileData>>>> partitionOnKeepActionsByFilename( List<Action<FileData>> actionList ) {

        if( actionList == null || actionList.isEmpty() ) {
            return Map.of(
                    Boolean.TRUE, Map.of(),
                    Boolean.FALSE, Map.of()
            );
        }

        return actionList.stream()
                .collect(
                    Collectors.partitioningBy(
                        a -> a instanceof KeepAction,
                        Collectors.groupingBy(
                            a -> a.data().filename()
                        )
                    )
                );
    }

    public static final PartitionResult partitionOnKeepActionItemsWithDuplicateFilenames( List<Action<FileData>> actionList ) {

        if( actionList == null || actionList.isEmpty() ) {
            return new PartitionResult( List.of(), List.of() );
        }

        List<Action<FileData>> negativeList = new LinkedList<>();

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
        // positive = all KeepAction items with duplicate filenames
        // negative = all other KeepAction items

        negativeList.addAll( partitionMap2.get( Boolean.FALSE ) );

        return new PartitionResult( partitionMap2.get( Boolean.TRUE ), negativeList );
    }


    public static final PartitionResult partitionOnDuplicateFilename( List<Action<FileData>> actionList ) {

        if( actionList == null || actionList.isEmpty() ) {
            return new PartitionResult( List.of(), List.of() );
        }

        var result = actionList.stream()
                .collect( Collectors.groupingBy( a -> a.data().filename() ) )
                .entrySet().stream()
                .collect( Collectors.partitioningBy( entry -> entry.getValue().size() > 1 ) );
        // positive = list of actions with duplicate filenames
        // negative = list of actions with unique filenames

        List<Action<FileData>> positiveList = new LinkedList<>();
        List<Action<FileData>> negativeList = new LinkedList<>();

        var actionsWithUniqueFilenamesList = result.get( Boolean.FALSE ).stream()
                .map( Map.Entry::getValue )
                .flatMap( List::stream )
                .collect( Collectors.toList() );

        negativeList.addAll( actionsWithUniqueFilenamesList );

        result.get( Boolean.TRUE ).stream()
                .map( entry -> entry.getValue() )
                .forEach( positiveList::addAll );

        return new PartitionResult(positiveList, negativeList);
    }


    public record PartitionResult( List<Action<FileData>> positivePartitionList, List<Action<FileData>> negativePartitionList ) {}
}