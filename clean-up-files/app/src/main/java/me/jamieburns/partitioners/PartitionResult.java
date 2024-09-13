package me.jamieburns.partitioners;

import java.util.List;

public record PartitionResult<T>( List<T> positivePartitionList, List<T> negativePartitionList ) {}
