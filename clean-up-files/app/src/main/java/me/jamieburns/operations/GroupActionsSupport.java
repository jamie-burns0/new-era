package me.jamieburns.operations;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.jamieburns.data.FileData;

public class GroupActionsSupport {

    public static final Map<Boolean, Map<String, List<Action<FileData>>>> partitionOnKeepActionsByFilename( List<Action<FileData>> actionList ) {

        if( actionList == null ) {
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


    public static final PartitionResult partitionOnUniqueness( Map<?, List<Action<FileData>>> groupedByMap ) {

        if( groupedByMap == null || groupedByMap.isEmpty() ) {
            return new PartitionResult( List.of(), Map.of() );
        }

        List<Action<FileData>> uniqueItemList = new LinkedList<>();
        Map<Object, List<Action<FileData>>> nonUniqueItemMap = new HashMap<>();

        groupedByMap.entrySet().stream()
                .forEach( e -> {
                    if( e.getValue().size() == 1 ) {
                        uniqueItemList.add( e.getValue().get(0) );
                    }
                    else {
                        nonUniqueItemMap.put( e.getKey(), e.getValue() );
                    }
                });

        return new PartitionResult( uniqueItemList, nonUniqueItemMap );
    }


    public record PartitionResult( List<Action<FileData>> positivePartitionList, Map<?, List<Action<FileData>>> negativePartitionMap ) {}
}