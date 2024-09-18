package me.jamieburns.mappers;

import java.util.List;
import java.util.stream.Collectors;

import me.jamieburns.actions.Action;
import me.jamieburns.actions.KeepAction;
import me.jamieburns.data.FileData;
import me.jamieburns.support.Timer;

public final class UniqueFileMapper implements Mapper {

    public static final List<Action<FileData>> toActionList( List<FileData> fileDataList ) {

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return List.of();
        }
        var timer = new Timer<>(UniqueFileMapper.class).start();
        List<Action<FileData>> result = fileDataList.stream()
                .map( item -> new KeepAction<>( item ) )
                .collect( Collectors.toList() );
        timer.stop();
        return result;
    }

}
