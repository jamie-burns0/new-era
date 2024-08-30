package me.jamieburns.operations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import me.jamieburns.data.FileData;
import me.jamieburns.operations.ActionSupport.Result;

public class ActionSupportTest {

    @Test
    void testActionListForUniqueFilesReturnsAnEmptyResultWhenArgumentIsNullOrEmpty() {

        assertThat( ActionSupport.actionListForUniqueFiles(null))
            .returns( Map.of(), from(Result::groupByMap))
            .returns( List.of(), from(Result::actionList));

        assertThat( ActionSupport.actionListForUniqueFiles(Map.of()))
            .returns( Map.of(), from(Result::groupByMap))
            .returns( List.of(), from(Result::actionList));
    }

    @Test
    void testActionListForUniqueFilesReturnsAnEmptyMapAndAFullListWhenArgumentContainsOnlySingleItemLists() {

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .build();

        var map = Stream.of(fd1, fd2)
                        .collect(
                            Collectors.groupingBy(FileData::filename));

        var result = ActionSupport.actionListForUniqueFiles(map);

        assertThat( result.groupByMap() ).isEmpty();

        assertThat( result.actionList() ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .map(Action::data) // List<FileData> (or stream of FileData ?)
                .containsExactlyInAnyOrder( fd1, fd2);
    }


    @Test
    void testActionListForUniqueFilesReturnsAFullMapAndAnEmptyListWhenArgumentContainsOnlyMultipleItemLists() {

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
                        .collect(
                            Collectors.groupingBy(FileData::sizeInBytes));

        var result = ActionSupport.actionListForUniqueFiles(map);

        assertThat( result.groupByMap() ) // Map<Long, List<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 1 )
                .values() // Collection<List<FileData>>
                .flatMap( v -> v) // List<FileData>
                .containsExactlyInAnyOrder( fd1, fd2);

        assertThat( result.actionList() ).isEmpty();
    }


    @Test
    void testActionListForUniqueFilesReturnsANonEmptyMapAndANonEmptyListWhenArgumentContainsBothSingleItemListsAndMultipleItemLists() {

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
                        .collect(
                            Collectors.groupingBy(FileData::sizeInBytes));

        var result = ActionSupport.actionListForUniqueFiles(map);

        assertThat( result.groupByMap() ) // Map<Long, List<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 1 )
                .values() // Collection<List<FileData>>
                .flatMap( v -> v) // List<FileData>
                .containsExactlyInAnyOrder( fd1, fd2);

        assertThat( result.actionList() ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 1 )
                .map(Action::data) // List<FileData> (or stream of FileData ?)
                .containsExactlyInAnyOrder( fd3 );
    }


    @Test
    void testActionListForDuplicateFilesReturnsAnEmptyResultWhenArgumentIsNullOrEmpty() {

        assertThat( ActionSupport.actionListForUniqueFiles(null))
            .returns( Map.of(), from(Result::groupByMap))
            .returns( List.of(), from(Result::actionList));

        assertThat( ActionSupport.actionListForUniqueFiles(Map.of()))
            .returns( Map.of(), from(Result::groupByMap))
            .returns( List.of(), from(Result::actionList));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testActionListForDuplicateFilesReturnsAFullMapAndAnEmptyListWhenArgumentContainsOnlySingleItemLists() {

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .build();

        var map = Stream.of(fd1, fd2)
                        .collect(
                            Collectors.groupingBy(FileData::filename));

        var result = ActionSupport.actionListForDuplicateFiles(map);

        assertThat((Map<String, List<FileData>>) result.groupByMap()) // Map<String, List<FileData>>
            .isNotNull()
            .isNotEmpty()
            .hasSize(2)
            .containsExactlyInAnyOrderEntriesOf(Map.of("file1", List.of(fd1), "file2", List.of(fd2)));

        assertThat( result.actionList() ).isEmpty();
    }


    @Test
    void testActionListForDuplicateFilesReturnsAnEmptyMapAndAFullListWhenArgumentContainsOnlyMultipleItemLists() {

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .sizeInBytes(1L)
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .sizeInBytes(1L)
                .build();

        var map = Stream.of(fd1, fd2)
                        .collect(
                            Collectors.groupingBy(FileData::sizeInBytes));

        var result = ActionSupport.actionListForDuplicateFiles(map);

        assertThat( result.groupByMap() ).isEmpty();

        assertThat( result.actionList() ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .map(Action::data) // List<FileData> (or stream of FileData ?)
                .containsExactlyInAnyOrder( fd1, fd2);
    }


    @SuppressWarnings("unchecked")
    @Test
    void testActionListForDuplicateFilesReturnsANonEmptyMapAndANonEmptyListWhenArgumentContainsBothSingleItemListsAndMultipleItemLists() {

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
                        .collect(
                            Collectors.groupingBy(FileData::sizeInBytes));

        var result = ActionSupport.actionListForDuplicateFiles(map);

        assertThat((Map<Long, List<FileData>>) result.groupByMap()) // Map<String, List<FileData>>
            .isNotNull()
            .isNotEmpty()
            .hasSize(1)
            .containsExactlyInAnyOrderEntriesOf(Map.of(singleItemSize, List.of(fd3)));

        assertThat( result.actionList() ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .map(Action::data) // List<FileData> (or stream of FileData ?)
                .containsExactlyInAnyOrder( fd1, fd2 );
    }
}
