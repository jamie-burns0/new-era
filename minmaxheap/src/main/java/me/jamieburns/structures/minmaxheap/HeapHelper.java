package me.jamieburns.structures.minmaxheap;

import java.util.function.BiPredicate;

public interface HeapHelper
{
    BiPredicate<Integer, Integer> compareChildWithParentPredicate();
    BiPredicate<Integer, Integer> compareChildrenWithEachOtherPredicate();

    default boolean shouldChildAndParentSwap( int childValue, int parentValue )
    {
        return compareChildWithParentPredicate().test( childValue, parentValue );
    }

    default boolean shouldLeftChildBeUsed( int leftChildValue, int rightChildValue )
    {
        return compareChildrenWithEachOtherPredicate().test( leftChildValue, rightChildValue );
    }
}
