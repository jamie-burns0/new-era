package me.jamieburns.operations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import me.jamieburns.data.FileData;
import me.jamieburns.operations.GroupFilesSupport.PartitionResult;

public class GroupFilesSupport_PartitionTest {

    @Test
    void whenArgumentIsNullOrEmpty_PartitionOnUniqueness_ReturnsAnEmptyPartitionResult() {

        assertThat( GroupFilesSupport.partitionOnUniqueness( null ) )
                .returns( List.of(), from(PartitionResult::positivePartitionList))
                .returns( Map.of(), from(PartitionResult::negativePartitionMap));

        assertThat( GroupFilesSupport.partitionOnUniqueness( Map.of() ) )
                .returns( List.of(), from(PartitionResult::positivePartitionList))
                .returns( Map.of(), from(PartitionResult::negativePartitionMap));
    }


    @Test
    void whenArgumentContainsOnlySingleItemLists_PartitionOnUniqueness_ReturnsAFullListAndAnEmptyMap() {

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .build();

        var map = Stream.of(fd1, fd2)
                        .collect(Collectors.groupingBy(FileData::filename));

        var result = GroupFilesSupport.partitionOnUniqueness( map );

        assertThat( result.positivePartitionList() ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .containsExactlyInAnyOrder( fd1, fd2);

        assertThat( result.negativePartitionMap() ).isEmpty();
    }


    @Test
    void whenArgumentContainsOnlyMultipleItemLists_PartitionOnUniqueness_ReturnsAnEmptyListAndAFullMap() {

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .sizeInBytes(1)
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .sizeInBytes(1)
                .build();

        var map = Stream.of(fd1, fd2)
                        .collect(Collectors.groupingBy(FileData::sizeInBytes));

        var result = GroupFilesSupport.partitionOnUniqueness(map);

        assertThat( result.positivePartitionList() ).isEmpty();

        assertThat( result.negativePartitionMap() ) // Map<Long, List<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 1 )
                .values() // Collection<List<FileData>>
                .flatMap( v -> v) // List<FileData>
                .containsExactlyInAnyOrder( fd1, fd2);
    }


    @Test
    void whenArgumentContainsBothSingleItemListsAndMultipleItemLists_PartitionOnUniqueness_ReturnsANonEmptyListAndANonEmptyMap() {

        var singleItemSize = 1L;
        var multipleItemSize = 2L;

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .sizeInBytes(multipleItemSize)
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .sizeInBytes(multipleItemSize)
                .build();

        var fd3 = new FileData.Builder()
                .filename("file3")
                .path("path3")
                .sizeInBytes(singleItemSize)
                .build();

        var map = Stream.of(fd1, fd2, fd3)
                        .collect(Collectors.groupingBy(FileData::sizeInBytes));

        var result = GroupFilesSupport.partitionOnUniqueness(map);

        assertThat( result.positivePartitionList() ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 1 )
                .containsExactlyInAnyOrder( fd3 );

        assertThat( result.negativePartitionMap() ) // Map<Long, List<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 1 )
                .values() // Collection<List<FileData>>
                .flatMap( v -> v) // List<FileData>
                .containsExactlyInAnyOrder( fd1, fd2);
    }


    @Test
    void whenArgumentIsNullOrEmpty_PartitionOnZeroLengthFileSize_ReturnsAnEmptyPartitionResult() {

        assertThat( GroupFilesSupport.partitionOnZeroLengthFileSize( null ) )
                .returns( List.of(), from(PartitionResult::positivePartitionList))
                .returns( Map.of(), from(PartitionResult::negativePartitionMap));

        assertThat( GroupFilesSupport.partitionOnZeroLengthFileSize( Map.of() ) )
                .returns( List.of(), from(PartitionResult::positivePartitionList))
                .returns( Map.of(), from(PartitionResult::negativePartitionMap));
    }


    @Test
    void whenArgumentContainsOnlyAZeroLengthFileSizeList_PartitionOnZeroLengthFileSize_ReturnsAFullListAndAnEmptyMap() {

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

        var map = Stream.of(fd1, fd2)
                        .collect(Collectors.groupingBy(FileData::sizeInBytes));

        var result = GroupFilesSupport.partitionOnZeroLengthFileSize( map );

        assertThat( result.positivePartitionList() ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .containsExactlyInAnyOrder( fd1, fd2);

        assertThat( result.negativePartitionMap() ).isEmpty();
    }


    @Test
    void whenArgumentContainsNoZeroLengthFileSizeList_PartitionOnZeroLengthFileSize_ReturnsAnEmptyListFullListAndAFullMap() {

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

        var map = Stream.of(fd1, fd2)
                        .collect(Collectors.groupingBy(FileData::sizeInBytes));

        var result = GroupFilesSupport.partitionOnZeroLengthFileSize( map );

        assertThat( result.positivePartitionList() ).isEmpty();

        assertThat( result.negativePartitionMap() )
                .isNotNull()
                .isNotEmpty()
                .hasSize( 1 )
                .values() // Collection<List<FileData>>
                .flatMap( v -> v) // List<FileData>
                .containsExactlyInAnyOrder( fd1, fd2);
    }
    
    
    @Test
    void whenArgumentContainsBothZeroAndNonZeroLengthFileSizeLists_PartitionOnZeroLengthFileSize_ReturnsAnListOfZeroLengthFileSizeItemsAndAMapOfNonZeroLengthFileSizeItems() {
    
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
    
            var map = Stream.of(fd1, fd2, fd3)
                            .collect(Collectors.groupingBy(FileData::sizeInBytes));
    
            var result = GroupFilesSupport.partitionOnZeroLengthFileSize( map );
    
            assertThat( result.positivePartitionList() ) // List<Action<FileData>>
                    .isNotNull()
                    .isNotEmpty()
                    .hasSize( 2 )
                    .containsExactlyInAnyOrder( fd1, fd2 );
    
            assertThat( result.negativePartitionMap() ) // Map<Long, List<FileData>>
                    .isNotNull()
                    .isNotEmpty()
                    .hasSize( 1 )
                    .values() // Collection<List<FileData>>
                    .flatMap( v -> v) // List<FileData>
                    .containsExactlyInAnyOrder( fd3 );
    }
}