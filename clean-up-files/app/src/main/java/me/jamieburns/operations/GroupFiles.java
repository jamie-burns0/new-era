package me.jamieburns.operations;

import java.util.List;
import java.util.Map;

import me.jamieburns.data.FileData;

public interface GroupFiles<K> {

    public Map<K, List<FileData>> groupFiles( List<FileData> fileDataList  );
    public Map<K, List<FileData>> regroupFiles( Map<?, List<FileData>> fileDataGroupedByMap );
}