package me.jamieburns;

import java.io.File;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.stream.Collectors;

import me.jamieburns.data.FileData;
import me.jamieburns.operations.Action;
import me.jamieburns.operations.ActionSupport;
import me.jamieburns.operations.FolderSupport;
import me.jamieburns.operations.GroupActionsSupport;
import me.jamieburns.operations.GroupFilesSupport;
import me.jamieburns.operations.KeepAction;
import me.jamieburns.operations.RemoveAction;

public class CleanUpFiles {

    public static void main(String[] args) {

        var actionList = buildActionList();

        var moveActionCount = actionList.stream()
            .filter(action -> action instanceof KeepAction)
            .count();

        var removeActionCount = actionList.stream()
            .filter(action -> action instanceof RemoveAction)
            .count();

        System.out.println("Generated \n%s MoveAction\n%s RemoveAction".formatted(moveActionCount, removeActionCount));

        actionList.stream()
                //.filter( action -> action instanceof RemoveAction )
                .forEach( a -> System.out.println( "[%s], %s, (%s bytes), %s, [%s]".formatted(
                        a.getClass().getSimpleName(),
                        a.data().filename(),
                        a.data().sizeInBytes(),
                        a.data().path(),
                        a.data().hash()
                )));
    }

    private static List<Action<FileData>> buildActionList() {

        // String path = "../..";
        // String filenameFilter = "^web.xml$";

        String path = "/mnt/c/Users/Jamie/OneDrive/Pictures-hp";
        //String path = "/home/jamie/tmp";
        String filenameFilter = "[Jj][Pp][Gg]$";

        // get our list of files from walking the folder tree

        List<FileData> fileDataList =
            FolderSupport.fileStream( new File( path ), filenameFilter )
                .collect(Collectors.toList());

        // pass one
        // - group files by file size
        // - add actions for unique files and remove them from the map

        Map<Long, List<FileData>> filesGroupedBySizeMap = GroupFilesSupport.groupFilesBySize( fileDataList );

        System.out.println( "Pass 1: [filesGroupedBySizeMap.size=%s]".formatted( filesGroupedBySizeMap.size()));

        var actionList = new LinkedList<Action<FileData>>();

        var partitionResult = GroupFilesSupport.partitionOnZeroLengthFileSize( filesGroupedBySizeMap );
        actionList.addAll( ActionSupport.actionListForZeroLengthFiles(partitionResult.positivePartitionList()));

        System.out.println( "Pass 1: partitionResult.zeroLengthItemsList.size=%s".formatted( partitionResult.positivePartitionList().size()));
        System.out.println( "Pass 1: partitionResult.nonZeroLengthItemsMap.size=%s".formatted( partitionResult.negativePartitionMap().size()));
        System.out.println( "Pass 1: actionList.size=%s".formatted( actionList.size()));

        partitionResult = GroupFilesSupport.partitionOnUniqueness( partitionResult.negativePartitionMap() );
        actionList.addAll( ActionSupport.actionListForUniqueFiles( partitionResult.positivePartitionList() ) );

        System.out.println( "Pass 1: partitionResult.uniqueItemsList.size=%s".formatted( partitionResult.positivePartitionList().size()));
        System.out.println( "Pass 1: partitionResult.nonUniqueItemsMap.size=%s".formatted( partitionResult.negativePartitionMap().size()));
        System.out.println( "Pass 1: actionList.size=%s".formatted( actionList.size()));

        // if there are no more entries in our map, we are done
        if( partitionResult.negativePartitionMap().size() == 0 ) {
            return actionList;
        }

        // pass two
        // - group files by hashing a chunk of the file contents
        // - add actions unique files and remove them from the map

        partitionResult = GroupFilesSupport.partitionOnUniqueness( GroupFilesSupport.regroupFilesByChunkHash(partitionResult.negativePartitionMap()) );
        actionList.addAll( ActionSupport.actionListForUniqueFiles( partitionResult.positivePartitionList() ) );

        System.out.println( "Pass 2: partitionResult.uniqueItemsList.size=%s".formatted( partitionResult.positivePartitionList().size()));
        System.out.println( "Pass 2: partitionResult.nonUniqueItemsMap.size=%s".formatted( partitionResult.negativePartitionMap().size()));
        System.out.println( "Pass 2: actionList.size=%s".formatted( actionList.size()));

        // if there are no more entries in our map, we are done
        if( partitionResult.negativePartitionMap().size() == 0 ) {
            return actionList;
        }

        // pass three
        // - group files by hashing the full file contents
        // - add actions unique files and remove them from the map

        partitionResult = GroupFilesSupport.partitionOnUniqueness(GroupFilesSupport.regroupFilesByFullHash(partitionResult.negativePartitionMap()));
        actionList.addAll( ActionSupport.actionListForUniqueFiles( partitionResult.positivePartitionList() ) );

        System.out.println( "Pass 3: partitionResult.uniqueItemsList.size=%s".formatted( partitionResult.positivePartitionList().size()));
        System.out.println( "Pass 3: partitionResult.nonUniqueItemsMap.size=%s".formatted( partitionResult.negativePartitionMap().size()));

        System.out.println( "Pass 3: actionList.size=%s".formatted( actionList.size()));

        // if there are no more entries in our map, we are done
        if( partitionResult.negativePartitionMap().size() == 0 ) {
            return actionList;
        }

        // pass four
        // - any entries remaining in our filesGroupedByFullHashMap are duplicates
        // - add actions for duplicate files

        actionList.addAll( ActionSupport.actionListForDuplicateFiles( partitionResult.negativePartitionMap() ) );

        System.out.println( "Pass 4: actionList.size=%s".formatted( actionList.size()));


        // pass five
        // - for any KeepAction with the same filename as another KeepAction,
        // - keep the first KeepAction and change the others to KeepWithRenameAction

        var actionList2 = new LinkedList<Action<FileData>>();

        var partitionMap = GroupActionsSupport.partitionOnKeepActionsByFilename( actionList );

        // put all the actions we wont change - ie actions other than KeepAction - into our new action list
        actionList2.addAll( ActionSupport.actionListFromGroupedActionList( partitionMap.get(Boolean.FALSE) ) );

        // for each KeepAction, partition into those with unique filenames and those with duplicate filenames
        var partitionResult2 = GroupActionsSupport.partitionOnUniqueness( partitionMap.get(Boolean.TRUE) );

        // put all the actions we wont change - ie KeepActions with unique filenames - into our new action list
        actionList2.addAll( partitionResult2.positivePartitionList() );

        // transform our list of KeepAction with duplicate filenames into a new Action list
        actionList2.addAll( ActionSupport.actionListForKeepActionWithDuplicateFilename( partitionResult2.negativePartitionMap() ));

        // System.out.println( "Pass 5: partitionResult.uniqueItemsList.size=%s".formatted( partitionResult2.positivePartitionList().size()));
        // System.out.println( "Pass 5: partitionResult.nonUniqueItemsMap.size=%s".formatted( partitionResult2.negativePartitionMap().size()));

        System.out.println( "Pass 5: actionList.size=%s".formatted( actionList.size()));

        return actionList;
    }

    /*
     My approach was to incrementally split files into buckets, throwing out any buckets containing only one file.

     1. Group files based on size. Discard groups containing only one entry.
     2. Divide up each group into smaller groups based on the first X kilobytes of each file. Discard groups which now contain only one entry.
        (Tune the X based on the block size for your filesystem.)
     3. Either hash all remaining files in series and use that to do the final dividing up into groups
        (best performance on rotating platter drives) or open all the files in a bucket and then read and compare
        chunks from them in parallel (bit-for-bit equality but needs an SSD to avoid crazy seek time overhead) to
        subdivide into new buckets. Discard groups containing only one entry.
     4. You now have your sets of duplicates.

     See https://www.reddit.com/r/rust/comments/ii2mjh/fast_and_accurate_hash_function_for_hashing_file/
     */
}
