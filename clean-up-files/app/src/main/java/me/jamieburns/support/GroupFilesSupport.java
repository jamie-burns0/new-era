package me.jamieburns.support;

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
        var timer = new Timer<>( "GroupFilesSupport.groupFilesByHash" ).start();
        var result = fileDataList.stream()
                .filter( fd -> fd.hash().isPresent() ) // we drop any FileData that has no hash
                .collect( Collectors.groupingBy( fd -> fd.hash().get() ) );
        timer.stop();
        return result;
    }
}
