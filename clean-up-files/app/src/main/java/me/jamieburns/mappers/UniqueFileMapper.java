package me.jamieburns.mappers;

import java.util.List;
import java.util.stream.Collectors;

import me.jamieburns.actions.Action;
import me.jamieburns.actions.KeepAction;
import me.jamieburns.data.FileData;

public final class UniqueFileMapper implements Mapper {

    public static final List<Action<FileData>> toActionList( List<FileData> fileDataList ) {

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return List.of();
        }

        return fileDataList.stream()
                .map( item -> new KeepAction<>( item ) )
                .collect( Collectors.toList() );
    }

}
