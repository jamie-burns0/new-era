package me.jamieburns.partitioners;

import java.util.List;
import java.util.stream.Collectors;

import me.jamieburns.data.FileData;

public final class ZeroLengthFileSizePartitioner implements Partitioner {

    public static final PartitionResult<FileData> partition( List<FileData> fileDataList ) {

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return new PartitionResult<>( List.of(), List.of() );
        }

        var result = fileDataList.stream()
                .collect( Collectors.partitioningBy( fd -> fd.sizeInBytes() == 0 ) );

        return new PartitionResult<>( result.get(Boolean.TRUE), result.get(Boolean.FALSE) );
        // positive = list of files with no content (zero-length files)
        // negative = list of all other files
    }
}
