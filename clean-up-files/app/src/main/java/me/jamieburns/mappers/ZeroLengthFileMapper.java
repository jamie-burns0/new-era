package me.jamieburns.mappers;

import java.util.List;
import java.util.stream.Collectors;

import me.jamieburns.actions.Action;
import me.jamieburns.actions.RemoveAction;
import me.jamieburns.data.FileData;
import me.jamieburns.support.Timer;

public final class ZeroLengthFileMapper implements Mapper {

    public static final List<Action<FileData>> toActionList( List<FileData> fileDataList ) {

        if( fileDataList == null || fileDataList.isEmpty() ) {
            return List.of();
        }
        var timer = new Timer<>(ZeroLengthFileMapper.class).start();
        List<Action<FileData>> result = fileDataList.stream()
                .filter( fd -> fd.sizeInBytes() == 0 )
                .map( item -> new RemoveAction<FileData>( item ) )
                .collect( Collectors.toList() );
        timer.stop();
        return result;
    }

}
