package me.jamieburns.operations;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import me.jamieburns.data.FileData;

public class ActionSupportTest {

    @Test
    void whenArgumentIsANullOrEmptyList_ActionListForZeroLengthFiles_ReturnsAnEmptyList() {
        assertThat( ActionSupport.actionListForZeroLengthFiles( null )).isEmpty();
        assertThat( ActionSupport.actionListForZeroLengthFiles( List.of() )).isEmpty();
    }


    @Test
    void whenArgumentIsAListOfFileDataWhereAllFilesAreZeroLength_actionListForZeroLengthFiles_ReturnsEachItemWrappedInARemoveAction() {

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .sizeInBytes( 0L )
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .sizeInBytes( 0L )
                .build();

        assertThat( ActionSupport.actionListForZeroLengthFiles( List.of( fd1, fd2 ) ) ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .allMatch(action -> action instanceof RemoveAction)
                .map(Action::data) // List<FileData> (or stream of FileData ?)
                .containsExactlyInAnyOrder( fd1, fd2);
    }


    @Test
    void whenArgumentIsAListOfFileDataWhereSomeFilesAreZeroLength_actionListForZeroLengthFiles_ReturnsEachZeroLengthItemWrappedInARemoveAction() {

        var zeroLengthFile = 0L;
        var nonZeroLengthFile = 1L;

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .sizeInBytes( zeroLengthFile )
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .sizeInBytes( nonZeroLengthFile )
                .build();

        var fd3 = new FileData.Builder()
                .filename("file3")
                .path("path3")
                .sizeInBytes( zeroLengthFile )
                .build();

        assertThat( ActionSupport.actionListForZeroLengthFiles( List.of( fd1, fd2, fd3 ) ) ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .allMatch(action -> action instanceof RemoveAction)
                .map(Action::data) // List<FileData> (or stream of FileData ?)
                .containsExactlyInAnyOrder( fd1, fd3 );
    }


    @Test
    void whenArgumentIsANullOrEmptyListActionListForUniqueFilesReturnsAnEmptyList() {
        assertThat( ActionSupport.actionListForUniqueFiles( null )).isEmpty();
        assertThat( ActionSupport.actionListForUniqueFiles( List.of() )).isEmpty();
    }


    @Test
    void whenArgumentIsAListOfFileData_actionListForUniqueFiles_ReturnsEachItemWrappedInAKeepAction() {

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .build();

        assertThat( ActionSupport.actionListForUniqueFiles( List.of( fd1, fd2 ) ) ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .allMatch(action -> action instanceof KeepAction)
                .map(Action::data) // List<FileData> (or stream of FileData ?)
                .containsExactlyInAnyOrder( fd1, fd2);
    }


    @Test
    void whenArgumentIsANullOrEmptyMap_actionListForDuplicateFiles_ReturnsAnEmptyList() {
        assertThat( ActionSupport.actionListForDuplicateFiles( null )).isEmpty();
        assertThat( ActionSupport.actionListForDuplicateFiles( Map.of() )).isEmpty();
    }


    @Test
    void whenArgumentContainsOnlySingleItemLists_actionListForDuplicateFiles_ReturnsEachListItemWrappedInAKeepAction() {

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

        assertThat( ActionSupport.actionListForDuplicateFiles( map ) ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .allMatch(action -> action instanceof KeepAction)
                .map(Action::data) // List<FileData> (or stream of FileData ?)
                .containsExactlyInAnyOrder( fd1, fd2);
    }


    @Test
    void whenArgumentContainsOnlyMultipleItemLists_actionListForDuplicateFiles_ReturnsEachListItemWrappedInAKeepActionOrARemoveAction() {

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
                        .collect(Collectors.groupingBy(FileData::sizeInBytes));

        var actionList = ActionSupport.actionListForDuplicateFiles(map);

        assertThat( actionList ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 );

        assertThat( actionList )
                .extracting( "class" )
                .containsExactly( KeepAction.class, RemoveAction.class );

        assertThat( actionList )
                .map(Action::data) // List<FileData> (or stream of FileData ?)
                .containsExactly( fd1, fd2);
    }


    @Test
    void whenArgumentContainsBothSingleItemListsAndMultipleItemLists_actionListForDuplicateFiles_ReturnsEachItemWrappedInAKeepActionOrARemoveAction() {

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

        var actionList = ActionSupport.actionListForDuplicateFiles(map);

        assertThat( actionList ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 3 );

        assertThat( actionList )
                .extracting( "class" )
                .containsExactly( KeepAction.class, KeepAction.class, RemoveAction.class );

        assertThat( actionList )
                .map(Action::data) // List<FileData> (or stream of FileData ?)
                .containsExactly( fd3, fd1, fd2);
    }


    @Test
    void whenArgumentIsNullOrEmpty_actionListForKeepActionWithDuplicateFilename_ReturnsAnEmptyList() {
        assertThat( ActionSupport.actionListForKeepActionWithDuplicateFilename(null)).isEmpty();
        assertThat( ActionSupport.actionListForKeepActionWithDuplicateFilename(List.of())).isEmpty();
    }


    @Test
    void whenArgumentContainsOnlySingleItemKeepActionLists_actionListForKeepActionWithDuplicateFilename_ReturnsEachItemWrappedInAKeepAction() {

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .sizeInBytes( 1L )
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .sizeInBytes( 2L )
                .build();

        var list = Stream.of(fd1, fd2)
                        .map( KeepAction::new )
                        .map(a -> (Action<FileData>) a)
                        .collect(Collectors.toList());

        assertThat( ActionSupport.actionListForKeepActionWithDuplicateFilename( list ) ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .allMatch(action -> action instanceof KeepAction)
                .map(Action::data) // List<FileData> (or stream of FileData ?)
                .containsExactlyInAnyOrder( fd1, fd2);
    }

    @Test
    void whenArgumentContainsOnlyMultipleKeepActionItemLists_actionListForKeepActionWithDuplicateFilename_ReturnsEachItemWrappedInAKeepActionOrKeepWithRenameAction() {

        var duplicateFilename = "filen";

        var fd1 = new FileData.Builder()
                .filename(duplicateFilename)
                .path("path1")
                .sizeInBytes( 1L )
                .build();

        var fd2 = new FileData.Builder()
                .filename(duplicateFilename)
                .path("path2")
                .sizeInBytes( 2L )
                .build();

        var list = Stream.of(fd1, fd2)
                        .map( KeepAction::new )
                        .map(a -> (Action<FileData>) a)
                        .collect(Collectors.toList());

        var actionList = ActionSupport.actionListForKeepActionWithDuplicateFilename( list );

        assertThat( actionList ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .map(Action::data) // List<FileData> (or stream of FileData ?)
                .containsExactlyInAnyOrder( fd1, fd2);

        assertThat( actionList )
                .extracting( "class" )
                .containsExactly( KeepAction.class, KeepWithRenameAction.class );

        assertThat( actionList )
                .map(Action::data) // List<FileData> (or stream of FileData ?)
                .containsExactly( fd1, fd2);
    }


    @Test
    void whenArgumentContainsSingleItemListsAndMultipleItemKeepActionLists_actionListForKeepActionWithDuplicateFilename_ReturnsEachItemWrappedInAKeepActionOrAKeepWithRenameAction() {

        var duplicateFilename = "fileD";
        var uniqueFilename = "fileU";

        var fd1 = new FileData.Builder()
                .filename(duplicateFilename)
                .path("path1")
                .sizeInBytes( 1L )
                .build();

        var fd2 = new FileData.Builder()
                .filename(duplicateFilename)
                .path("path2")
                .sizeInBytes( 2L )
                .build();

        var fd3 = new FileData.Builder()
                .filename( uniqueFilename )
                .path( "path3")
                .sizeInBytes( 3L )
                .build();

        var list = Stream.of(fd1, fd2, fd3)
                        .map( KeepAction::new )
                        .map(a -> (Action<FileData>) a)
                        .collect(Collectors.toList());

        var actionList = ActionSupport.actionListForKeepActionWithDuplicateFilename( list );

        assertThat( actionList ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 3 );

        assertThat( actionList )
                .extracting( "class" )
                .containsExactly( KeepAction.class, KeepWithRenameAction.class, KeepAction.class );

        assertThat( actionList )
                .map(Action::data) // List<FileData> (or stream of FileData ?)
                .containsExactlyInAnyOrder( fd1, fd2, fd3 );
    }



    @Test
    void whenArgumentIsNullOrEmpty_actionListForUniqueFileWithDuplicateFilename_ReturnsAnEmptyList() {
        assertThat( ActionSupport.actionListForUniqueFilesWithDuplicateFilename(null)).isEmpty();
        assertThat( ActionSupport.actionListForUniqueFilesWithDuplicateFilename(List.of())).isEmpty();
    }


    @Test
    void whenArgumentContainsOnlySingleItemLists_actionListForUniqueFileWithDuplicateFilename_ReturnsEachItemWrappedInAKeepAction() {

        var fd1 = new FileData.Builder()
                .filename("file1")
                .path("path1")
                .sizeInBytes( 1L )
                .build();

        var fd2 = new FileData.Builder()
                .filename("file2")
                .path("path2")
                .sizeInBytes( 2L )
                .build();

        var list = List.of(fd1, fd2);

        assertThat( ActionSupport.actionListForUniqueFilesWithDuplicateFilename( list ) ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .allMatch(action -> action instanceof KeepAction)
                .map(Action::data) // List<FileData> (or stream of FileData ?)
                .containsExactlyInAnyOrder( fd1, fd2);
    }


    @Test
    void whenArgumentContainsOnlyMultipleItemLists_actionListForUniqueFileWithDuplicateFilename_ReturnsEachItemWrappedInAKeepActionOrAKeepWithRenameAction() {

        var duplicateFilename = "fileD";

        var fd1 = new FileData.Builder()
                .filename(duplicateFilename)
                .path("path1")
                .sizeInBytes( 1L )
                .build();

        var fd2 = new FileData.Builder()
                .filename(duplicateFilename)
                .path("path2")
                .sizeInBytes( 2L )
                .build();

        var list = List.of(fd1, fd2);

        var actionList = ActionSupport.actionListForUniqueFilesWithDuplicateFilename( list );

        assertThat( actionList ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 );

        assertThat( actionList )
                .extracting( "class" )
                .containsExactly( KeepAction.class, KeepWithRenameAction.class );

        assertThat( actionList )
                .map(Action::data) // List<FileData> (or stream of FileData ?)
                        .containsExactlyInAnyOrder( fd1, fd2 );
    }


    @Test
    void whenArgumentContainsBothSingleItemListsAndMultipleItemLists_actionListForUniqueFileWithDuplicateFilename_ReturnsEachItemWrappedInAKeepActionOrAKeepWithRenameAction() {

        var duplicateFilename = "fileD";
        var uniqueFilename = "fileU";

        var fd1 = new FileData.Builder()
                .filename(duplicateFilename)
                .path("path1")
                .sizeInBytes( 1L )
                .build();

        var fd2 = new FileData.Builder()
                .filename(duplicateFilename)
                .path("path2")
                .sizeInBytes( 2L )
                .build();

        var fd3 = new FileData.Builder()
                .filename( uniqueFilename )
                .path( "path3")
                .sizeInBytes( 3L )
                .build();

        var list = List.of(fd1, fd2, fd3);

        var actionList = ActionSupport.actionListForUniqueFilesWithDuplicateFilename( list );

        assertThat( actionList ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 3 );

        assertThat( actionList )
                .extracting( "class" )
                .containsExactly( KeepAction.class, KeepWithRenameAction.class, KeepAction.class );

        assertThat( actionList )
                .map(Action::data) // List<FileData> (or stream of FileData ?)
                .containsExactlyInAnyOrder( fd1, fd2, fd3 );
    }
}
