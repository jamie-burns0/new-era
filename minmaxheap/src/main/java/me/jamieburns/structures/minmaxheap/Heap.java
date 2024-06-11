package me.jamieburns.structures.minmaxheap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Heap
{
    public enum HeapType
    {
        MIN, MAX
    }

    /*
     * we are using an ArrayList instead of an array so that
     * we don't have to be concerned about the underlying
     * storage capacity
     */
    private List<Integer> heap = new ArrayList<>();
    private int heapSize = 0;

    private HeapHelper heapHelper;

    public Heap( HeapType heapType )
    {
        heap.add( 0 ); // unused slot so that we can have a 1-indexed array

        heapHelper = switch( heapType )
            {
                case MIN -> new MinHeapHelper();
                case MAX -> new MaxHeapHelper();
            };
    }

    private void addToHeapTail( int value )
    {
        heap.add( value );
        heapSize++;
    }

    // Add an element to the heap
    public void insert(int value)
    {
        addToHeapTail( value );

        if( heapSize == 1 )
            return;

        var childIndex = heapSize;

        heapifyUpFrom( childIndex );
    }

    public Optional<Integer> remove()
    {
        if( heapSize == 0 )
        {
            return Optional.empty();
        }

        var heapTailValue = heap.remove( heapSize-- );

        if( heapSize == 0 )
        {
            return Optional.of(heapTailValue); // tail was also the head element
        }

        var heapHeadValue = heap.set( 1, heapTailValue );

        heapifyDownFrom( 1 );

        return Optional.of(heapHeadValue);
    }

    private void heapifyUpFrom( int childIndex )
    {
        if( childIndex == 1 )
        {
            return;
        }

        var parentIndex = childIndex >>> 1; // unsigned right-shift operator equivalent to dividing by 2

        if( heapHelper.shouldChildAndParentSwap( getValueAt( childIndex ), getValueAt( parentIndex ) ) )
        {
            swap( childIndex, parentIndex );
            heapifyUpFrom( parentIndex );
        }
    }

    private void heapifyDownFrom( int parentIndex )
    {
        var leftChildIndex = parentIndex << 1; // left-shift operator equivalent to multiplying by 2

        if( leftChildIndex > heapSize )
        {
            return;
        }

        if( leftChildIndex < heapSize )
        {
            heapifyDownWithBothChildren( parentIndex );
        }
        else
        {
            heapifyDownWithLeftChildOnly( parentIndex );
        }
    }

    private void heapifyDownWithLeftChildOnly( int parentIndex )
    {
        var leftChildIndex = parentIndex << 1;

        if( heapHelper.shouldChildAndParentSwap( getValueAt( leftChildIndex ), getValueAt( parentIndex ) ) )
        {
            swapWithParent( leftChildIndex );
            heapifyDownFrom( leftChildIndex );
        }
    }

    private void heapifyDownWithBothChildren( int parentIndex )
    {
        var indexOfLeftChild = parentIndex << 1;
        var indexOfRightChild = indexOfLeftChild + 1;

        var parentValue = getValueAt( parentIndex );
        var valueOfLeftChild = getValueAt( indexOfLeftChild );
        var valueOfRightChild = getValueAt( indexOfRightChild );

        var indexOfChildToUse = 0;
        var valueOfChildToUse = 0;

        if( heapHelper.shouldLeftChildBeUsed( valueOfLeftChild, valueOfRightChild ) )
        {
            indexOfChildToUse = indexOfLeftChild;
            valueOfChildToUse = valueOfLeftChild;
        }
        else
        {
            indexOfChildToUse = indexOfRightChild;
            valueOfChildToUse = valueOfRightChild;
        }

        if( heapHelper.shouldChildAndParentSwap( valueOfChildToUse, parentValue ) )
        {
            swapWithParent( indexOfChildToUse );
            heapifyDownFrom( indexOfChildToUse );
        }
    }

    private void swapWithParent( int childIndex )
    {
        swap( childIndex, childIndex >>> 1 );
    }

    // Swap elements at two indices in the heap
    private void swap( int childIndex, int parentIndex )
    {
        var childValue = heap.set( childIndex, getValueAt( parentIndex ) );
        heap.set( parentIndex, childValue );
    }

    private int getValueAt( int index )
    {
        return heap.get( index );
    }


    public static void main(String[] args) {
        Heap heap = new Heap( HeapType.MAX );
        heap.insert(10);
        heap.insert(5);
        heap.insert(15);
        heap.insert(3);

        System.out.println("Extracted min value: " + heap.remove()); // Should print 3 for HeapType.MIN,, 15 for HeapType.MAX
    }
}
    /*
    
    Feel free to modify or extend this class as needed for your specific use case! 😊
    
    Source: Conversation with Bing, 29/02/2024
    (1) Heap Data Structure - GeeksforGeeks. https://www.geeksforgeeks.org/heap-data-structure/.
    (2) Java's Heap Data Structure | CodingDrills. https://www.codingdrills.com/tutorial/heap-data-structure/heaps-in-java.
    (3) Is there a Heap in java? - Stack Overflow. https://stackoverflow.com/questions/14165325/is-there-a-heap-in-java.
    (4) Heap - Data Structures and Algorithms (DSA) Guide. https://dsaguide.com/heap-data-structure-guide/.
    (5) Heap Data Structure - Programiz. https://www.programiz.com/dsa/heap-data-structure.
    */
