package me.jamieburns.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.jamieburns.data.FileData;

public final class GroupFilesBySize implements GroupFiles<Long> {

    @Override
    public Map<Long, List<FileData>> groupFiles(List<FileData> fileDataList) {

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return Map.<Long, List<FileData>>of();
        }

        return fileDataList.stream()
            .collect(Collectors.groupingBy(FileData::sizeInBytes));
    }

    /**
     * For reference only. Implementation of groupFiles where the returned map is mutable.
     * The map returned in the groupFiles implementation above is not guaranteed to be mutable.
     */
    Map<Long, List<FileData>> groupFilesMutableMapImpl( List<FileData> fileDataList ) {

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return Map.<Long, List<FileData>>of();
        }

        return fileDataList.stream()
                .collect(Collectors.toMap(
                        FileData::sizeInBytes,
                        Collections::singletonList,
                        (list1, list2) -> {
                                List<FileData> mergedList = new ArrayList<>(list1);
                                mergedList.addAll(list2);
                                return mergedList;
                        }
                ));
    }

    @Override
    public Map<Long, List<FileData>> regroupFiles(Map<?, List<FileData>> fileDataGroupedByAnything) {

        if( fileDataGroupedByAnything == null || fileDataGroupedByAnything.isEmpty() ) {
            return Map.<Long, List<FileData>>of();
        }

        Map<Long, List<FileData>> fileDataRegroupedByHash = new HashMap<>();

        for( var fileDataList : fileDataGroupedByAnything.values() ) {

            Map<Long, List<FileData>> regroupedFileData = groupFiles( fileDataList );

            for( var key : regroupedFileData.keySet() ) {                
                fileDataRegroupedByHash.computeIfAbsent( key, unused -> new ArrayList<FileData>() );
                fileDataRegroupedByHash.get( key ).addAll( regroupedFileData.get( key ) );
            }
        }

        return fileDataRegroupedByHash;
    }


    /**
     * For reference only. Implementation of regroupFiles using Stream and Collectors apis.
     */
    Map<Long, List<FileData>> regroupFilesStreamAndCollectorsImpl(Map<?, List<FileData>> fileDataGroupedByAnything) {

        if( fileDataGroupedByAnything == null || fileDataGroupedByAnything.isEmpty() ) {
            return Map.<Long, List<FileData>>of();
        }

        return fileDataGroupedByAnything.values().stream()
                .flatMap(fileDataList -> groupFiles(fileDataList).entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> {
                            existing.addAll(replacement);
                            return existing;
                        },
                        HashMap::new
                ));
    }
    
}
