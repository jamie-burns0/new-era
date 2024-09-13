package me.jamieburns.partitioners;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.jamieburns.data.FileData;

public final class UniqueFileSizePartitioner implements Partitioner {

    public static final PartitionResult<FileData> partition( List<FileData> fileDataList ) {

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return new PartitionResult<>(List.of(), List.of());
        }

        var map = fileDataList.stream()
                .collect( Collectors.groupingBy( FileData::sizeInBytes ))
                .values().stream()
                .collect( Collectors.partitioningBy( list -> list.size() == 1 ) )
                .entrySet().stream()
                .collect( Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .flatMap( List::stream )
                                .collect( Collectors.toList() )
                        )
                );

        return new PartitionResult<>( map.get(Boolean.TRUE), map.get(Boolean.FALSE) );
        // positive = list of unique files based on file size
        // negative = list of all other files
    }

}
