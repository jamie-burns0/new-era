package me.jamieburns.operations;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import me.jamieburns.data.FileData;

public class GroupFilesBySizeTest {

    @Test
    public void shouldReturnAMapOfFileDataGroupedBySize() {

        var size1 = 1L;
        var size2 = 2L;

        var fd1 = new FileData.Builder()
            .filename( "fn1" )
            .path( "p1" )
            .sizeInBytes(size1)
            .build();

        var fd2 = new FileData.Builder()
            .filename( "fn2" )
            .path( "p2" )
            .sizeInBytes(size2)
            .build();
        
        var fd3 = new FileData.Builder()
            .filename( "fn3" )
            .path( "p3" )
            .sizeInBytes(size1)
            .build();
        
        var list = List.of( fd1, fd2, fd3 );

        var map = new GroupFilesBySize().groupFiles( list );

        assertThat( map ).hasSize( 2 );

        assertThat( map ).containsKey( size1 );
        assertThat( map ).extractingByKey( size1, as(InstanceOfAssertFactories.LIST)).hasSize( 2 );
        assertThat( map ).extractingByKey( size1, as(InstanceOfAssertFactories.LIST)).containsExactlyInAnyOrder( fd1, fd3 );


        assertThat( map ).containsKey( size2 );
        assertThat( map ).extractingByKey( size2, as(InstanceOfAssertFactories.LIST)).hasSize( 1 );
        assertThat( map ).extractingByKey( size2, as(InstanceOfAssertFactories.LIST)).containsExactlyInAnyOrder( fd2 );
    }


    @Test
    public void noListShouldReturnAnEmptyMap() {
        assertThat( new GroupFilesBySize().groupFiles(null) ).isEmpty();
        assertThat( new GroupFilesBySize().groupFiles(List.<FileData>of()) ).isEmpty();
    }


    @Test
    public void aListWithOnlyOneItemShouldReturnAMapWithOnlyOneEntryContainingTheOnlyListItem() {

        var size = 1L;

        var fd = new FileData.Builder()
            .filename( "fn1" )
            .path( "p1" )
            .sizeInBytes(size)
            .build();

        var list = List.of( fd );

        var map = new GroupFilesBySize().groupFiles( list );

        assertThat( map ).hasSize( 1 );

        assertThat( map ).containsKey( size );
        assertThat( map ).extractingByKey( size, as(InstanceOfAssertFactories.LIST)).hasSize( 1 );
        assertThat( map ).extractingByKey( size, as(InstanceOfAssertFactories.LIST)).containsExactlyInAnyOrder( fd );
    }


    @Test
    public void aListWhereAllItemsHaveTheSameFileSizeShouldReturnAMapWithOnlyOneEntryContainingAllTheListItems() {
            
            var size = 1L;
    
            var fd1 = new FileData.Builder()
                .filename( "fn1" )
                .path( "p1" )
                .sizeInBytes(size)
                .build();
    
            var fd2 = new FileData.Builder()
                .filename( "fn2" )
                .path( "p2" )
                .sizeInBytes(size)
                .build();
    
            var fd3 = new FileData.Builder()
                .filename( "fn3" )
                .path( "p3" )
                .sizeInBytes(size)
                .build();
    
            var list = List.of( fd1, fd2, fd3 );
    
            var map = new GroupFilesBySize().groupFiles( list );
    
            assertThat( map ).hasSize( 1 );
    
            assertThat( map ).containsKey( size );
            assertThat( map ).extractingByKey( size, as(InstanceOfAssertFactories.LIST)).hasSize( list.size() );
            assertThat( map ).extractingByKey( size, as(InstanceOfAssertFactories.LIST)).containsExactlyInAnyOrder( fd1, fd2, fd3 );
            
            // order is important when asserting that one list is equal to another
            assertThat( List.of( fd1, fd2 )).isEqualTo( List.of( fd1, fd2 ) );
            assertThat( List.of( fd2, fd1 )).isNotEqualTo( List.of( fd1, fd2 ) );            
    }
}
