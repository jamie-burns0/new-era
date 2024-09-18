package me.jamieburns.partitioners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

import org.junit.jupiter.api.Test;

import me.jamieburns.data.FileDataBuilder;
import java.util.List;

public class UniqueHashPartitionerTest {

    @Test
    void whenArgumentIsNullOrEmpty_partitionOnUniqueHash_ReturnsAnEmptyPartitionResult() {

        assertThat( UniqueHashPartitioner.partition( null ) )
                .returns( List.of(), from(PartitionResult::positivePartitionList))
                .returns( List.of(), from(PartitionResult::negativePartitionList));

        assertThat( UniqueHashPartitioner.partition( List.of() ) )
                .returns( List.of(), from(PartitionResult::positivePartitionList))
                .returns( List.of(), from(PartitionResult::negativePartitionList));
    }


    @Test
    void whenArgumentContainsOnlyItemsWithUniqueHashes_partitionOnUniqueHash_ReturnsAPartitionResultWithAnEmptyNegativeListAndAllItemsInThePositiveList() {

        var uniqueHash1 = "hash1";
        var uniqueHash2 = "hash2";
        var uniqueHash3 = "hash3";

        var fd1 = new FileDataBuilder()
                .filename("file1")
                .path("path1")
                .sizeInBytes(1L)
                .hash(uniqueHash1)
                .build();

        var fd2 = new FileDataBuilder()
                .filename("file2")
                .path("path2")
                .sizeInBytes(2L)
                .hash(uniqueHash2)
                .build();

        var fd3 = new FileDataBuilder()
                .filename("file3")
                .path("path3")
                .sizeInBytes(3L)
                .hash(uniqueHash3)
                .build();

        var result = UniqueHashPartitioner.partition( List.of(fd1, fd2, fd3) );

        assertThat( result.positivePartitionList() )
                .isNotNull()
                .isNotEmpty()
                .hasSize( 3 )
                .containsExactlyInAnyOrder( fd1, fd2, fd3 );

        assertThat( result.negativePartitionList() ).isEmpty();
    }


    @Test
    void whenArgumentContainsOnlyItemsWithDuplicateHashes_partitionOnUniqueHash_ReturnsAPartitionResultWithAnEmptyPositiveListAndAllItemsInTheNegativeList() {

        var duplicateHash = "duplicatehash";

        var fd1 = new FileDataBuilder()
                .filename("file1")
                .path("path1")
                .sizeInBytes(1L)
                .hash(duplicateHash)
                .build();

        var fd2 = new FileDataBuilder()
                .filename("file2")
                .path("path2")
                .sizeInBytes(1L)
                .hash(duplicateHash)
                .build();

        var fd3 = new FileDataBuilder()
                .filename("file3")
                .path("path3")
                .sizeInBytes(1L)
                .hash(duplicateHash)
                .build();

        var result = UniqueHashPartitioner.partition( List.of(fd1, fd2, fd3) );

        assertThat( result.positivePartitionList() ).isEmpty();

        assertThat( result.negativePartitionList() )
                .isNotNull()
                .isNotEmpty()
                .hasSize( 3 )
                .containsExactlyInAnyOrder( fd1, fd2, fd3 );
    }


    @Test
    void whenArgumentContainsItemsWithUniqueFileSizesAndItemsWithDuplicateFileSizes_partitionOnUniqueHash_ReturnsAPartitionResultWithAllUniqueItemsInThePositiveListAndAllDuplicateItemsInTheNegativeList() {

        var duplicateHash = "duplicatehash";
        var uniqueHash1 = "uniqueHash1";
        var uniqueHash2 = "uniqueHash2";

        var fd1 = new FileDataBuilder()
                .filename("file1")
                .path("path1")
                .sizeInBytes(1L)
                .hash(duplicateHash)
                .build();

        var fd2 = new FileDataBuilder()
                .filename("file2")
                .path("path2")
                .sizeInBytes(2L)
                .hash(uniqueHash1)
                .build();

        var fd3 = new FileDataBuilder()
                .filename("file3")
                .path("path3")
                .sizeInBytes(3L)
                .hash(duplicateHash)
                .build();

        var fd4 = new FileDataBuilder()
                .filename("file4")
                .path("path4")
                .sizeInBytes(4L)
                .hash(uniqueHash2)
                .build();

                var result = UniqueHashPartitioner.partition( List.of(fd1, fd2, fd3, fd4) );

        assertThat( result.positivePartitionList() )
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .containsExactlyInAnyOrder( fd2, fd4 );

        assertThat( result.negativePartitionList() )
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .containsExactlyInAnyOrder( fd1, fd3 );
    }
}