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
    void whenArgumentIsAListOfFileDataWhereAllFilesAreZeroLength_ActionListForZeroLengthFiles_ReturnsEachItemWrappedInARemoveAction() {

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
    void whenArgumentIsAListOfFileDataWhereSomeFilesAreZeroLength_ActionListForZeroLengthFiles_ReturnsEachZeroLengthItemWrappedInARemoveAction() {

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
    void whenArgumentIsAListOfFileDataActionListForUniqueFilesReturnsEachItemWrappedInAKeepAction() {

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
    void whenArgumentIsANullOrEmptyMapActionListForDuplicateFilesReturnsAnEmptyList() {
        assertThat( ActionSupport.actionListForDuplicateFiles( null )).isEmpty();
        assertThat( ActionSupport.actionListForDuplicateFiles( Map.of() )).isEmpty();
    }


    @Test
    void whenArgumentContainsOnlySingleItemListsActionListForDuplicateFilesReturnsEachListItemWrappedInAKeepAction() {

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
    void whenArgumentContainsOnlyMultipleItemListsActionListForDuplicateFilesReturnsEachListItemWrappedInAKeepActionOrARemoveAction() {

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
    void whenArgumentContainsBothSingleItemListsAndMultipleItemLists_ActionListForDuplicateFiles_ReturnsEachItemWrappedInAKeepActionOrARemoveAction() {

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
    void whenArgumentIsNullOrEmpty_ActionListForKeepActionWithDuplicateFilename_ReturnsAnEmptyList() {
        assertThat( ActionSupport.actionListForKeepActionWithDuplicateFilename(null)).isEmpty();
        assertThat( ActionSupport.actionListForKeepActionWithDuplicateFilename(Map.of())).isEmpty();
    }


    @Test
    void whenArgumentContainsOnlySingleItemKeepActionLists_ActionListForKeepActionWithDuplicateFilename_ReturnsEachItemWrappedInAKeepAction() {

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

        var map = Stream.of(fd1, fd2)
                        .map( KeepAction::new )
                        .map(a -> (Action<FileData>) a)
                        .collect(Collectors.groupingBy( a -> a.data().filename() ) );

        assertThat( ActionSupport.actionListForKeepActionWithDuplicateFilename( map ) ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .allMatch(action -> action instanceof KeepAction)
                .map(Action::data) // List<FileData> (or stream of FileData ?)
                .containsExactlyInAnyOrder( fd1, fd2);
    }

    @Test
    void whenArgumentContainsOnlyMultipleKeepActionItemLists_ActionListForKeepActionWithDuplicateFilename_ReturnsEachItemWrappedInAKeepAction() {

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

        var map = Stream.of(fd1, fd2)
                        .map( KeepAction::new )
                        .map(a -> (Action<FileData>) a)
                        .collect(Collectors.groupingBy( a -> a.data().filename() ) );

        var actionList = ActionSupport.actionListForKeepActionWithDuplicateFilename( map );

        assertThat( actionList ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 2 )
                .allMatch(action -> action instanceof KeepAction)
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
    void whenArgumentContainsSingleItemListsAndMultipleItemKeepActionLists_ActionListForKeepActionWithDuplicateFilename_ReturnsEachItemWrappedInAKeepActionOrAKeepWithRenameAction() {

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

        var map = Stream.of(fd1, fd2, fd3)
                        .map( KeepAction::new )
                        .map(a -> (Action<FileData>) a)
                        .collect(Collectors.groupingBy( a -> a.data().filename() ) );

        var actionList = ActionSupport.actionListForKeepActionWithDuplicateFilename( map );

        assertThat( actionList ) // List<Action<FileData>>
                .isNotNull()
                .isNotEmpty()
                .hasSize( 3 );

        assertThat( actionList )
                .extracting( "class" )
                .containsExactly( KeepAction.class, KeepWithRenameAction.class );

        assertThat( actionList )
                .map(Action::data) // List<FileData> (or stream of FileData ?)
                .containsExactlyInAnyOrder( fd1, fd2, fd3 );
    }


    @Test
    void whenArgumentIsNullOrEmpty_ActionListFromGroupedActionList_ReturnsAnEmptyList() {
        assertThat( ActionSupport.actionListFromGroupedActionList(null)).isEmpty();
        assertThat( ActionSupport.actionListFromGroupedActionList(Map.of())).isEmpty();
    }


    @Test
    void whenArgumentContainsAnyGroupedActionList_ActionListFromGroupedActionList_ReturnsAllActionsInAList() {

        var a1 = new KeepAction<>( new FileData.Builder()
                .filename("file1")
                .path("path1")
                .sizeInBytes( 1L )
                .build()
        );

        var a2 = new KeepWithRenameAction<>( new FileData.Builder()
                .filename("file2")
                .path("path2")
                .sizeInBytes( 2L )
                .build()
        );

        var a3 = new RemoveAction<>( new FileData.Builder()
                .filename( "file3" )
                .path( "path3")
                .sizeInBytes( 3L )
                .build()
        );

        Map<String, List<Action<FileData>>> map = Map.of(
                a1.data().filename(), List.of( a1 ),
                a2.data().filename(), List.of( a2 ),
                a3.data().filename(), List.of( a3 )
        );

        var actionList = ActionSupport.actionListFromGroupedActionList(map);

        assertThat( actionList )
                .isNotNull()
                .isNotEmpty()
                .hasSize( 3 )
                .containsExactlyInAnyOrder( a1, a2, a3 );
    }
}
