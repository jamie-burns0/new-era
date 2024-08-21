package me.jamieburns;

import java.io.File;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import me.jamieburns.data.FileData;
import me.jamieburns.operations.Action;
import me.jamieburns.operations.ActionSupport;
import me.jamieburns.operations.FolderSupport;
import me.jamieburns.operations.GroupFilesByChunkHash;
import me.jamieburns.operations.GroupFilesByFullHash;
import me.jamieburns.operations.GroupFilesBySize;
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
                .forEach( a -> System.out.println( "[%s], %s, (%s bytes), %s, [%s], [%s]".formatted(
                        a.getClass().getSimpleName(),
                        a.data().filename(),
                        a.data().sizeInBytes(),
                        a.data().path(),
                        a.data().chunkHash(),
                        a.data().fullHash()
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
            new FolderSupport().fileStream( new File( path ), filenameFilter )
                .collect(Collectors.toList());

        // pass one
        // - group files by file size
        // - add actions for unique files and remove them from the map

        Map<Long, List<FileData>> filesGroupedBySizeMap = new GroupFilesBySize().groupFiles( fileDataList );
        
        System.out.println( "Pass 1: [filesGroupedBySizeMap.size=%s]".formatted( filesGroupedBySizeMap.size()));

        var actionList = new ArrayList<Action<FileData>>();

        var result = new ActionSupport().actionListForUniqueFiles( filesGroupedBySizeMap );

        System.out.println( "Pass 1: result.groupByMap.size=%s".formatted( result.groupByMap().size()));
        System.out.println( "Pass 1: result.actionList.size=%s".formatted( result.actionList().size()));

        actionList.addAll( result.actionList() );

        System.out.println( "Pass 1: actionList.size=%s".formatted( actionList.size()));

        // if there are no more entries in our map, we are done
        if( result.groupByMap().size() == 0 ) {
            return actionList;
        }

        // pass two
        // - group files by hashing a chunk of the file contents
        // - add actions unique files and remove them from the map

        Map<String, List<FileData>> filesGroupedByChunkHashMap = new GroupFilesByChunkHash().regroupFiles(result.groupByMap());

        result = new ActionSupport().actionListForUniqueFiles(filesGroupedByChunkHashMap);

        System.out.println( "Pass 2: result.groupByMap.size=%s".formatted( result.groupByMap().size()));
        System.out.println( "Pass 2: result.actionList.size=%s".formatted( result.actionList().size()));

        actionList.addAll( result.actionList() );

        System.out.println( "Pass 2: actionList.size=%s".formatted( actionList.size()));

        // if there are no more entries in our map, we are done
        if( result.groupByMap().size() == 0 ) {
            return actionList;
        }

        // pass three
        // - group files by hashing the full file contents
        // - add actions unique files and remove them from the map

        Map<String, List<FileData>> filesGroupedByFullHashMap = new GroupFilesByFullHash().regroupFiles(result.groupByMap());
        
        result = new ActionSupport().actionListForUniqueFiles(filesGroupedByFullHashMap);

        System.out.println( "Pass 3: result.groupByMap.size=%s".formatted( result.groupByMap().size()));
        System.out.println( "Pass 3: result.actionList.size=%s".formatted( result.actionList().size()));

        actionList.addAll( result.actionList() );

        System.out.println( "Pass 3: actionList.size=%s".formatted( actionList.size()));

        // if there are no more entries in our map, we are done
        if( result.groupByMap().size() == 0 ) {
            return actionList;
        }

        // pass four
        // - any entries remaining in our filesGroupedByFullHashMap are duplicates
        // - add actions for duplicate files
        
        result = new ActionSupport().actionListForDuplicateFiles( filesGroupedByFullHashMap );

        System.out.println( "Pass 4: result.groupByMap.size=%s".formatted( result.groupByMap().size()));
        System.out.println( "Pass 4: result.actionList.size=%s".formatted( result.actionList().size()));

        actionList.addAll( result.actionList() );

        System.out.println( "Pass 4: actionList.size=%s".formatted( actionList.size()));

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
