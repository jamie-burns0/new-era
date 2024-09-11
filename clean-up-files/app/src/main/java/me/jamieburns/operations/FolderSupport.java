package me.jamieburns.operations;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.jamieburns.data.FileData;

public class FolderSupport {

    private static final Predicate<String> DEFAULT_FILENAME_PREDICATE = Pattern.compile( ".*" ).asPredicate();

    private FolderSupport() {}

    static final Stream<FileData> fileStream( File folder ) {
        return walkFolder( folder, DEFAULT_FILENAME_PREDICATE ).stream();
    }

    static final Stream<FileData> fileStream( File folder, String matchFilenameToRegex ) {
        return walkFolder( folder, Pattern.compile( matchFilenameToRegex ).asPredicate() ).stream();
    }

    private static final List<FileData> walkFolder( File folder, Predicate<String> filenamePredicate ) {

        var fileDataList = new ArrayList<FileData>();
        var folderStack = new LinkedList<File>();

        folderStack.push( folder );

        while ( folderStack.isEmpty() == false ) {

            var currentFolder = folderStack.pop();

            var residentFileDataList = buildFileDataList( currentFolder, filenamePredicate );

            Stream.of( currentFolder.listFiles() )
                  .filter( File::isDirectory )
                  .forEach( folderStack::push );

            fileDataList.addAll( residentFileDataList );
        }

        return fileDataList;
    }

    private static final List<FileData> buildFileDataList( File folder, Predicate<String> filenamePredicate ) {

        if( folder == null || folder.isDirectory() == false ) {
            return List.of(); // Empty folder
        }

        var fileDataSet =
            Stream.of( folder.listFiles() )
                  .filter(File::isFile)
                  .filter( f -> filenamePredicate.test( f.getName() ) )
                  .map( f -> new FileData.Builder()
                        .filename( f.getName() )
                        .path( f.getPath() )
                        .sizeInBytes( f.length() )
                        .build() )
                  .collect( Collectors.toList() );

        return fileDataSet;
    }
}
