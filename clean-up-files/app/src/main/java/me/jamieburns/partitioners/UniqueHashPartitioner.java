package me.jamieburns.partitioners;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.jamieburns.data.FileData;
import me.jamieburns.support.Timer;

public final class UniqueHashPartitioner implements Partitioner {

    public static final PartitionResult<FileData> partition( List<FileData> fileDataList ) {

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return new PartitionResult<>( List.of(), List.of() );
        }

        var timer = new Timer<>(UniqueHashPartitioner.class).start();
        var result = fileDataList.stream()
                .collect( Collectors.groupingBy( FileData::hash ) )
                .values().stream()
                .collect( Collectors.partitioningBy( list -> list.size() == 1) ) // partition on unique file size
                .entrySet().stream()
                .collect( Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .flatMap( List::stream )
                                .collect( Collectors.toList() )
                        )
                );
        timer.stop();
        return new PartitionResult<>( result.get( Boolean.TRUE ), result.get( Boolean.FALSE ) );
        // positive = list of files with a unique hash
        // negative = all other files
    }

}
