package me.jamieburns.partitioners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

import org.junit.jupiter.api.Test;

import me.jamieburns.data.FileData;

import java.util.List; // Add this import statement

public class ZeroLengthFileSizePartitionerTest {

    @Test
    void whenArgumentIsNullOrEmpty_PartitionOnZeroLengthFileSize_ReturnsAnEmptyPartitionResult() {

        assertThat( ZeroLengthFileSizePartitioner.partition( (List<FileData>) null ) )
                .returns( List.of(), from(PartitionResult::positivePartitionList))
                .returns( List.of(), from(PartitionResult::negativePartitionList));

        assertThat( ZeroLengthFileSizePartitioner.partition( List.of() ) )
                .returns( List.of(), from(PartitionResult::positivePartitionList))
                .returns( List.of(), from(PartitionResult::negativePartitionList));
    }


    @Test
    void whenArgumentContainsOnlyAZeroLengthFileSizeList_PartitionOnZeroLengthFileSize_ReturnsAFullPositiveListAndAnEmptyNegativeList() {

        var zeroLengthFileSize = 0L;

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .sizeInBytes( zeroLengthFileSize )
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .sizeInBytes( zeroLengthFileSize )
                .build();

        var list = List.of(fd1, fd2);

        var result = ZeroLengthFileSizePartitioner.partition( list );

        assertThat( result.positivePartitionList() ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .containsExactlyInAnyOrder( fd1, fd2);

        assertThat( result.negativePartitionList() ).isEmpty();
    }


    @Test
    void whenArgumentContainsNoZeroLengthFileSizeList_PartitionOnZeroLengthFileSize_ReturnsAnEmptyPositiveListAndAFullNegativeList() {

        var nonZeroLengthFileSize = 1L;

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .sizeInBytes( nonZeroLengthFileSize )
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .sizeInBytes( nonZeroLengthFileSize )
                .build();

        var list = List.of(fd1, fd2);

        var result = ZeroLengthFileSizePartitioner.partition( list );

        assertThat( result.positivePartitionList() ).isEmpty();

        assertThat( result.negativePartitionList() )
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .containsExactlyInAnyOrder( fd1, fd2);
    }


    @Test
    void whenArgumentContainsBothZeroAndNonZeroLengthFileSizeLists_PartitionOnZeroLengthFileSize_ReturnsAPositiveListContainingAllZeroLengthFileItemsAndANegativeListContainingAllNonZeroLengthFileItems() {

        var zeroLengthFileSize = 0L;
        var nonZeroLengthFileSize = 1L;

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .sizeInBytes( zeroLengthFileSize )
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .sizeInBytes( zeroLengthFileSize )
                .build();

        var fd3 = new FileData.Builder()
                .filename("file3")
                .path("path3")
                .sizeInBytes( nonZeroLengthFileSize )
                .build();

        var list = List.of(fd1, fd2, fd3);

        var result = ZeroLengthFileSizePartitioner.partition( list );

        assertThat( result.positivePartitionList() ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .containsExactlyInAnyOrder( fd1, fd2 );

        assertThat( result.negativePartitionList() ) // Map<Long, List<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 1 )
                .containsExactlyInAnyOrder( fd3 );
    }    
}