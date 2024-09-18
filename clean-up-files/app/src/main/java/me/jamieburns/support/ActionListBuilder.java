package me.jamieburns.support;

import java.util.LinkedList;
import java.util.List;

import me.jamieburns.actions.Action;
import me.jamieburns.data.FileData;
import me.jamieburns.mappers.DuplicateFilesMapper;
import me.jamieburns.mappers.KeepActionWithDuplicateFilenameMapper;
import me.jamieburns.mappers.UniqueFileMapper;
import me.jamieburns.mappers.ZeroLengthFileMapper;
import me.jamieburns.partitioners.KeepActionItemsWithDuplicateFilenamesPartitioner;
import me.jamieburns.partitioners.UniqueFileSizePartitioner;
import me.jamieburns.partitioners.UniqueHashPartitioner;
import me.jamieburns.partitioners.ZeroLengthFileSizePartitioner;

public class ActionListBuilder {

    /*
     d = duplicate
     u = unique
     z = zero

       | name | size | content | action
     1 |  d   |  d   |    d    | keep one, remove others
     2 |  d   |  d   |    u    | keep one of each unique content, remove others
     3 |  d   |  u   |    -    | keep one, keep others with a rename
     4 |  u   |  d   |    d    | keep one, remove others
     5 |  u   |  d   |    u    | keep one, keep others with a rename
     6 |  u   |  u   |    -    | keep all
     7 |  -   |  z   |    -    | remove all

    Use
     - size first
       - we know we can keep any file that has a unique size
       - duplicate size could be unique or duplicate files
     - content second
       - we know we can keep any file that has unique content
       - for any file with duplicate content, we know we can
         keep one and remove others
     - name of files we are keeping third
       - we know we can keep any file that has a unique name
       - for any file with a duplicate name, we know we can
         keep one as is, and keep others with a rename
    */

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

    static final List<Action<FileData>> buildActionList( List<FileData> fileDataList ) {

        var actionList = new LinkedList<Action<FileData>>();

        // handle files that have zero-length - case (7)

        var partitionResult = ZeroLengthFileSizePartitioner.partition(fileDataList);
        actionList.addAll( ZeroLengthFileMapper.toActionList(partitionResult.positivePartitionList() /* list of files with no content */ ));
        System.out.println( "[ActionListBuilder]: With zero-length files: actionList.size=%s".formatted( actionList.size()));


        // handle files that have a unique file size - case (6) + partial (3) (duplicate names handled later)

        partitionResult = UniqueFileSizePartitioner.partition( partitionResult.negativePartitionList() );
        actionList.addAll( UniqueFileMapper.toActionList( partitionResult.positivePartitionList() ) );
        System.out.println( "[ActionListBuilder]: With unique file size: actionList.size=%s".formatted( actionList.size()));


        // handle files that have a unique chunk hash - case (5) + partial (2) (duplicate names handled later)

        fileDataList = FileDataSupport.rebuildWithChunkHash( partitionResult.negativePartitionList() );
        partitionResult = UniqueHashPartitioner.partition( fileDataList );
        actionList.addAll( UniqueFileMapper.toActionList( partitionResult.positivePartitionList() ) );
        System.out.println( "[ActionListBuilder]: With unique chunk hash: actionList.size=%s".formatted( actionList.size()));


        // handle files that have a unique full hash - case (5) + partial (2) (duplicate names handled later)

        fileDataList = FileDataSupport.rebuildWithFullHash( partitionResult.negativePartitionList() );
        partitionResult = UniqueHashPartitioner.partition( fileDataList );
        actionList.addAll( UniqueFileMapper.toActionList( partitionResult.positivePartitionList() ) );
        System.out.println( "[ActionListBuilder]: With unique full hash: actionList.size=%s".formatted( actionList.size()));


        // handle files that have a duplicate full hash - case (4) + partial (1) (duplicate names handled later)

        var groupedByHashMap = GroupFilesSupport.groupFilesByHash( partitionResult.negativePartitionList() );
        actionList.addAll( DuplicateFilesMapper.toActionList( groupedByHashMap ) );
        System.out.println( "[ActionListBuilder]: With duplicate full hash: actionList.size=%s".formatted( actionList.size()));


        // handle any KeepAction items with the same filename as another KeepAction item

        var actionList2 = new LinkedList<Action<FileData>>();

        var partitionResult3 = KeepActionItemsWithDuplicateFilenamesPartitioner.partition( actionList );

        // put all the actions we wont change - ie actions other than KeepAction - into our new action list
        actionList2.addAll( partitionResult3.negativePartitionList() );
        System.out.println( "[ActionListBuilder]: With all KeepAction items that have unique filenames and all other Action items: actionList2.size=%s".formatted( actionList2.size()));

        // transform our list of KeepAction with duplicate filenames into a new Action list - partial (1), (2), (3) (duplicate names)

        actionList2.addAll( KeepActionWithDuplicateFilenameMapper.toActionList( partitionResult3.positivePartitionList() ));
        System.out.println( "[ActionListBuilder]: With all KeepAction items that have duplicate filenames: actionList2.size=%s".formatted( actionList2.size()));

        return actionList2;
    }
}
