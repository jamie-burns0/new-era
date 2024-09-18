package me.jamieburns.partitioners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

import java.util.List;

import org.junit.jupiter.api.Test;

import me.jamieburns.data.FileDataBuilder;

public class UniqueFileSizePartitionerTest {

    @Test
    void whenArgumentIsNullOrEmpty_partitionOnUniqueFileSizes_ReturnsAnEmptyPartitionResult() {

        assertThat( UniqueFileSizePartitioner.partition( null ) )
                .returns( List.of(), from(PartitionResult::positivePartitionList))
                .returns( List.of(), from(PartitionResult::negativePartitionList));

        assertThat( UniqueFileSizePartitioner.partition( List.of() ) )
                .returns( List.of(), from(PartitionResult::positivePartitionList))
                .returns( List.of(), from(PartitionResult::negativePartitionList));
    }


    @Test
    void whenArgumentContainsOnlyItemsWithUniqueFileSizes_partitionOnUniqueFileSizes_ReturnsAPartitionResultWithAnEmptyNegativeListAndAllItemsInThePositiveList() {

        var fd1 = new FileDataBuilder()
                .filename("file1")
                .path("path1")
                .sizeInBytes(1L)
                .build();

        var fd2 = new FileDataBuilder()
                .filename("file2")
                .path("path2")
                .sizeInBytes(2L)
                .build();

        var fd3 = new FileDataBuilder()
                .filename("file3")
                .path("path3")
                .sizeInBytes(3L)
                .build();

        var result = UniqueFileSizePartitioner.partition( List.of(fd1, fd2, fd3) );

        assertThat( result.positivePartitionList() ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 3 )
                .containsExactlyInAnyOrder( fd1, fd2, fd3 );

        assertThat( result.negativePartitionList() ).isEmpty();
    }


    @Test
    void whenArgumentContainsOnlyItemsWithDuplicateFileSizes_partitionOnUniqueFileSizes_ReturnsAPartitionResultWithAnEmptyPositiveListAndAllItemsInTheNegativeList() {

        var fd1 = new FileDataBuilder()
                .filename("file1")
                .path("path1")
                .sizeInBytes(1L)
                .build();

        var fd2 = new FileDataBuilder()
                .filename("file2")
                .path("path2")
                .sizeInBytes(1L)
                .build();

        var fd3 = new FileDataBuilder()
                .filename("file3")
                .path("path3")
                .sizeInBytes(1L)
                .build();

        var result = UniqueFileSizePartitioner.partition( List.of(fd1, fd2, fd3) );

        assertThat( result.positivePartitionList() ).isEmpty();

        assertThat( result.negativePartitionList() ) // List<FileData>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 3 )
                .containsExactlyInAnyOrder( fd1, fd2, fd3 );
    }


    @Test
    void whenArgumentContainsItemsWithUniqueFileSizesAndItemsWithDuplicateFileSizes_partitionOnUniqueFileSizes_ReturnsAPartitionResultWithAllUniqueItemsInThePositiveListAndAllDuplicateItemsInTheNegativeList() {

        var uniqueFileSize1 = 1L;
        var uniqueFileSize2 = 2L;
        var duplicateFileSize = 3L;

        var fd1 = new FileDataBuilder()
                .filename("file1")
                .path("path1")
                .sizeInBytes(uniqueFileSize1)
                .build();

        var fd2 = new FileDataBuilder()
                .filename("file2")
                .path("path2")
                .sizeInBytes(duplicateFileSize)
                .build();

        var fd3 = new FileDataBuilder()
                .filename("file3")
                .path("path3")
                .sizeInBytes(uniqueFileSize2)
                .build();

        var fd4 = new FileDataBuilder()
                .filename("file4")
                .path("path4")
                .sizeInBytes(duplicateFileSize)
                .build();

                var result = UniqueFileSizePartitioner.partition( List.of(fd1, fd2, fd3, fd4) );

        assertThat( result.positivePartitionList() ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .containsExactlyInAnyOrder( fd1, fd3 );

        assertThat( result.negativePartitionList() ) // List<FileData>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .containsExactlyInAnyOrder( fd2, fd4 );
    }
}