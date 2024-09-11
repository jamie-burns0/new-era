package me.jamieburns.operations;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.jamieburns.data.FileData;

public class GroupFilesSupport {

    private GroupFilesSupport() {}

    public static final Map<Long, List<FileData>> groupFilesBySize(List<FileData> fileDataList) {

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return Map.of();
        }

        return fileDataList.stream()
            .collect(Collectors.groupingBy(FileData::sizeInBytes));
    }


    /**
     * For reference only. Implementation of groupFiles where the returned map is mutable.
     * The map returned in the groupFiles implementation above is not guaranteed to be mutable.
     */
    // private static final Map<Long, List<FileData>> groupFilesBySizeInMutableMapImpl( List<FileData> fileDataList ) {

    //     if( fileDataList == null || fileDataList.isEmpty() ) {
    //         return Map.of();
    //     }

    //     return fileDataList.stream()
    //             .collect(Collectors.toMap(
    //                     FileData::sizeInBytes,
    //                     Collections::singletonList,
    //                     (list1, list2) -> {
    //                             List<FileData> mergedList = new ArrayList<>(list1);
    //                             mergedList.addAll(list2);
    //                             return mergedList;
    //                     }
    //             ));
    // }


    public static final Map<String, List<FileData>> groupFilesByFilename( List<FileData> fileDataList ) {

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return Map.of();
        }

        return fileDataList.stream()
            .collect( Collectors.groupingBy( FileData::filename ));
    }


    public static final Map<String, List<FileData>> groupFilesByHash( List<FileData> fileDataList ) {

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return Map.of();
        }

        return fileDataList.stream()
                .filter( fd -> fd.hash().isPresent() ) // we drop any FileData that has no hash
                .collect( Collectors.groupingBy( fd -> fd.hash().get() ) );
    }


    public static final PartitionResult partitionOnUniqueHash( List<FileData> fileDataList ) {

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return new PartitionResult( List.of(), List.of() );
        }

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
                // positive = list of files with a unique hash
                // negative = all other files

        return new PartitionResult( result.get( Boolean.TRUE ), result.get( Boolean.FALSE ) );
    }


    public static final PartitionResult partitionOnZeroLengthFileSize( List<FileData> fileDataList ) {

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return new PartitionResult( List.of(), List.of() );
        }

        var result = fileDataList.stream()
                .collect( Collectors.partitioningBy( fd -> fd.sizeInBytes() == 0 ) );

        return new PartitionResult( result.get(Boolean.TRUE), result.get(Boolean.FALSE) );
    }


    public static final PartitionResult partitionOnUniqueFileSizesWithDuplicateFilenames( List<FileData> fileDataList ) {

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return new PartitionResult(List.of(), List.of());
        }

        var negativeList = new LinkedList<FileData>();

        var partitionOnUniqueFileSizeMap = fileDataList.stream()
                .collect( Collectors.groupingBy( FileData::sizeInBytes ) )
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
                // positive = list of unique file sizes
                // negative = list of non-unique file sizes

        negativeList.addAll( partitionOnUniqueFileSizeMap.get( Boolean.FALSE ));

        var uniqueFileSizeGroupedOnDuplicateFilenameMap = partitionOnUniqueFileSizeMap.get( Boolean.TRUE ).stream()
                .collect( Collectors.groupingBy( FileData::filename ))
                .values().stream()
                .collect( Collectors.partitioningBy( list -> list.size() > 1 ) )
                .entrySet().stream()
                .collect( Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .flatMap( List::stream )
                                .collect( Collectors.toList() )
                ));
                // positive = list of unique file sizes with duplicate filenames
                // negative = list of unique file sizes with unique filenames

        negativeList.addAll( uniqueFileSizeGroupedOnDuplicateFilenameMap.get( Boolean.FALSE ) );

        return new PartitionResult( uniqueFileSizeGroupedOnDuplicateFilenameMap.get( Boolean.TRUE ), negativeList );
    }


    public static final PartitionResult partitionOnUniqueFileSizes( List<FileData> fileDataList ) {

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return new PartitionResult(List.of(), List.of());
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

        return new PartitionResult( map.get(Boolean.TRUE), map.get(Boolean.FALSE) );
    }


    public record PartitionResult( List<FileData> positivePartitionList, List<FileData> negativePartitionList ) {}
}
