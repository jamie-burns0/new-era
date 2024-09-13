package me.jamieburns.partitioners;

public sealed interface Partitioner
    permits ZeroLengthFileSizePartitioner, UniqueFileSizePartitioner, UniqueHashPartitioner, KeepActionItemsWithDuplicateFilenamesPartitioner {

}
