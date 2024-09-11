package me.jamieburns;

import java.util.LinkedList;
import java.util.List;

import me.jamieburns.CleanUpFilesMain.Args;
import me.jamieburns.data.FileData;
import me.jamieburns.operations.Action;
import me.jamieburns.operations.ActionSupport;
import me.jamieburns.operations.FilesSupport;
import me.jamieburns.operations.GroupActionsSupport;
import me.jamieburns.operations.GroupFilesSupport;

public class CleanUpFilesSupport {

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

    public final static List<Action<FileData>> buildActionList( Args args ) {

        // get our list of files from walking the folder tree

        var fileDataList = FilesSupport.buildFileList( args.path(), args.filenameFilter() );

        if( fileDataList.isEmpty() ) {
            return List.of();
        }

        System.out.println( "found %s files".formatted( fileDataList.size() ) );


        var actionList = new LinkedList<Action<FileData>>();


        // handle files that have zero-length

        var partitionResult = GroupFilesSupport.partitionOnZeroLengthFileSize(fileDataList);
        // positive = list of files with no content (zero-length files)
        // negative = list of all other files
        actionList.addAll( ActionSupport.actionListForZeroLengthFiles(partitionResult.positivePartitionList() /* list of files with no content */ ));
        System.out.println( "With zero-length files: actionList.size=%s".formatted( actionList.size()));


        // handle files that have a unique file size // and a unique filename

        partitionResult = GroupFilesSupport.partitionOnUniqueFileSizes( partitionResult.negativePartitionList() );
        // positive = list of unique files based on file size - duplicate filenames were handled above
        // negative = list of all other files
        actionList.addAll( ActionSupport.actionListForUniqueFiles( partitionResult.positivePartitionList() ) );
        System.out.println( "With unique file size: actionList.size=%s".formatted( actionList.size()));


        // handle files that have a unique chunk hash

        fileDataList = FilesSupport.rebuildWithChunkHash( partitionResult.negativePartitionList() );
        partitionResult = GroupFilesSupport.partitionOnUniqueHash( fileDataList );
        // positive = list of files with a unique chunk hash
        // negative = all other files
        actionList.addAll( ActionSupport.actionListForUniqueFiles( partitionResult.positivePartitionList() ) );
        System.out.println( "With unique chunk hash: actionList.size=%s".formatted( actionList.size()));


        // handle files that have a unique full hash

        fileDataList = FilesSupport.rebuildWithFullHash( partitionResult.negativePartitionList() );
        partitionResult = GroupFilesSupport.partitionOnUniqueHash( fileDataList );
        // positive = list of files with a unique full hash
        // negative = list of files with duplicate full hashes
        actionList.addAll( ActionSupport.actionListForUniqueFiles( partitionResult.positivePartitionList() ) );
        System.out.println( "With unique full hash: actionList.size=%s".formatted( actionList.size()));


        // handle files that have a duplicate full hash

        var groupedByHashMap = GroupFilesSupport.groupFilesByHash( partitionResult.negativePartitionList() );
        actionList.addAll( ActionSupport.actionListForDuplicateFiles( groupedByHashMap ) );
        System.out.println( "With duplicate full hash: actionList.size=%s".formatted( actionList.size()));


        // handle any KeepAction items with the same filename as another KeepAction item

        var actionList2 = new LinkedList<Action<FileData>>();

        var partitionResult3 = GroupActionsSupport.partitionOnKeepActionItemsWithDuplicateFilenames( actionList );
        // put all the actions we wont change - ie actions other than KeepAction - into our new action list
        actionList2.addAll( partitionResult3.negativePartitionList() );
        System.out.println( "With all KeepAction items that have unique filenames and all other Action items: actionList2.size=%s".formatted( actionList2.size()));

        // transform our list of KeepAction with duplicate filenames into a new Action list
        actionList2.addAll( ActionSupport.actionListForKeepActionWithDuplicateFilename( partitionResult3.positivePartitionList() ));
        System.out.println( "With all KeepAction items that have duplicate filenames: actionList2.size=%s".formatted( actionList2.size()));

        return actionList2;
    }
}
