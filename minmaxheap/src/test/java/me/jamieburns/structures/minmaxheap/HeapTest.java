package me.jamieburns.structures.minmaxheap;

import org.junit.jupiter.api.Test;

import me.jamieburns.structures.minmaxheap.Heap;
import me.jamieburns.structures.minmaxheap.Heap.HeapType;

import static org.assertj.core.api.Assertions.assertThat;

public class HeapTest {

    @Test
    void test1()
    {
        var  heap = new Heap( HeapType.MAX );

        heap.insert(10);
        heap.insert(5);
        heap.insert(15);
        heap.insert(3);

        assertThat( heap.remove() ).contains( 15 );
    }

    @Test
    void test2()
    {
        var  heap = new Heap( HeapType.MIN );

        heap.insert(10);
        heap.insert(5);
        heap.insert(15);
        heap.insert(3);

        assertThat( heap.remove() ).contains( 3 );
    }

    @Test
    void test3()
    {
        var  heap = new Heap( HeapType.MAX );

        heap.insert(10);
        heap.insert(5);
        heap.insert(15);
        heap.insert(3);

        assertThat( heap.remove() ).contains( 15 );
        assertThat( heap.remove() ).contains( 10 );
        assertThat( heap.remove() ).contains( 5 );
        assertThat( heap.remove() ).contains( 3 );
    }

    @Test
    void test4()
    {
        var  heap = new Heap( HeapType.MIN );

        heap.insert(10);
        heap.insert(5);
        heap.insert(15);
        heap.insert(3);

        assertThat( heap.remove() ).contains( 3 );
        assertThat( heap.remove() ).contains( 5 );
        assertThat( heap.remove() ).contains( 10 );
        assertThat( heap.remove() ).contains( 15 );
    }

    @Test
    void test5()
    {
        var  heap = new Heap( HeapType.MIN );

        heap.insert(3);
        heap.insert(3);
        heap.insert(3);
        heap.insert(3);

        assertThat( heap.remove() ).contains( 3 );
        assertThat( heap.remove() ).contains( 3 );
        assertThat( heap.remove() ).contains( 3 );
        assertThat( heap.remove() ).contains( 3 );
    }

    @Test
    void test6()
    {
        var  heap = new Heap( HeapType.MIN );

        heap.insert(3);

        assertThat( heap.remove() ).contains( 3 );
    }

    @Test
    void test7()
    {
        var  heap = new Heap( HeapType.MIN );

        heap.insert(3);

        assertThat( heap.remove() ).contains( 3 );
        assertThat( heap.remove() ).isEmpty();
    }

    @Test
    void test8()
    {
        var  heap = new Heap( HeapType.MIN );

        assertThat( heap.remove() ).isEmpty();
    }
}
