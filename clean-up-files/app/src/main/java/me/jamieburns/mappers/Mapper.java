package me.jamieburns.mappers;

public sealed interface Mapper
        permits ZeroLengthFileMapper, UniqueFileMapper, DuplicateFilesMapper, KeepActionWithDuplicateFilenameMapper {

}
