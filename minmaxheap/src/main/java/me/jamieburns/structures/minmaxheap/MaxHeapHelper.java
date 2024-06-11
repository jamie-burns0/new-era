package me.jamieburns.structures.minmaxheap;

import java.util.function.BiPredicate;

public class MaxHeapHelper implements HeapHelper
{
    public BiPredicate<Integer, Integer> compareChildWithParentPredicate()
    {
        return ( childValue, parentValue ) -> childValue > parentValue ;
    }

    public BiPredicate<Integer, Integer> compareChildrenWithEachOtherPredicate()
    {
        return ( leftChildValue, rightChildValue ) -> leftChildValue > rightChildValue;
    }
}
