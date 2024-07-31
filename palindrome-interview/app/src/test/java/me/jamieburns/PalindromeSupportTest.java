package me.jamieburns;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import java.util.TreeMap;

class PalindromeSupportTest {

    private PalindromeSupport ps;

    @BeforeEach
    void setUp() {
        ps = new PalindromeSupport();
    }

    @Test
    void nullStringIsNotAPalindromeCandidate() {
        assertTrue(ps.isNotPalindromeCandidate((String) null));
    }

    @Test
    void emptyStringIsNotAPalindromeCandidate() {
        assertTrue(ps.isNotPalindromeCandidate(""));
    }

    @Test
    void anyAlphabeticOnlyStringIsAPalindromeCandidate() {
        assertFalse(ps.isNotPalindromeCandidate("a"));
        assertFalse(ps.isNotPalindromeCandidate("ab"));
        assertFalse(ps.isNotPalindromeCandidate("abc"));
    }

    @Test
    void anyStringWithNonAlphabeticCharactersIsNotAPalindromeCandidate() {
        assertTrue(ps.isNotPalindromeCandidate("1"));
        assertTrue(ps.isNotPalindromeCandidate("a1"));
        assertTrue(ps.isNotPalindromeCandidate("="));
        assertTrue(ps.isNotPalindromeCandidate("=a"));
        assertTrue(ps.isNotPalindromeCandidate("."));
        assertTrue(ps.isNotPalindromeCandidate("a."));
    }

    @Test
    void frequencyMapWhereMoreThanOneCharacterIsNotPairedIsNotAPalindromCandidate() {
        assertTrue(ps.isNotPalindromeCandidate(Map.of('a', 1L, 'b', 3L)));
        assertTrue(ps.isNotPalindromeCandidate(Map.of('a', 4L, 'b', 3L, 'c', 101L)));
    }

    @Test
    void frequencyMapWhereOnlyOneCharacterIsNotPairedIsAPalindromCandidate() {
        assertFalse(ps.isNotPalindromeCandidate(Map.of('a', 2L, 'b', 1L)));
        assertFalse(ps.isNotPalindromeCandidate(Map.of('a', 101L, 'b', 300L)));
    }

    @Test
    void frequencyMapWhereAllCharactersArePairedIsAPalindromCandidate() {
        assertFalse(ps.isNotPalindromeCandidate(Map.of('a', 2L, 'b', 4L, 'c', 6L)));
        assertFalse(ps.isNotPalindromeCandidate(Map.of('a', 20L, 'b', 400L, 'c', 6000L)));
        assertFalse(ps.isNotPalindromeCandidate(Map.of('a', 2000L)));
    }

    @Test
    void frequencyOfCharactersMapToPalindromeWithOneUnpairedCharacterReturnsPalindrome() {
        // aaabbbbbccccba
        assertEquals("aabbccbccbbaa",
                ps.frequencyOfCharactersMapToPalindrome(new TreeMap<>(Map.of('a', 4L, 'b', 5L, 'c', 4L))));

        // abbaa
        assertEquals("ababa", ps.frequencyOfCharactersMapToPalindrome(new TreeMap<>(Map.of('a', 3L, 'b', 2L))));

        // aab
        assertEquals("aba", ps.frequencyOfCharactersMapToPalindrome(new TreeMap<>(Map.of('a', 2L, 'b', 1L))));

        // a
        assertEquals("a", ps.frequencyOfCharactersMapToPalindrome(new TreeMap<>(Map.of('a', 1L))));

        // aaa
        assertEquals("aaa", ps.frequencyOfCharactersMapToPalindrome(new TreeMap<>(Map.of('a', 3L))));
    }

    @Test
    void frequencyOfCharactersMapToPalindromeWithAllCharactersPairedReturnsPalindrome() {
        // aabbcc
        assertEquals("abbccccccbba",
                ps.frequencyOfCharactersMapToPalindrome(new TreeMap<>(Map.of('a', 2L, 'b', 4L, 'c', 6L))));

        // aabbccdd
        assertEquals("aaabccccbaaa",
                ps.frequencyOfCharactersMapToPalindrome(new TreeMap<>(Map.of('a', 6L, 'b', 2L, 'c', 4L))));

        // aa
        assertEquals("aa", ps.frequencyOfCharactersMapToPalindrome(new TreeMap<>(Map.of('a', 2L))));

        // aaaaaa
        assertEquals("aaaaaa", ps.frequencyOfCharactersMapToPalindrome(new TreeMap<>(Map.of('a', 6L))));
    }

    @Test
    void makePalidromeFromNullStringReturnsEmptyOptional() {
        assertTrue(ps.makePalindromeFrom(null).isEmpty());
    }

    @Test
    void makePalidromeFromEmptyStringReturnsEmptyOptional() {
        assertTrue(ps.makePalindromeFrom("").isEmpty());
    }

    @Test
    void makePalindromeFromStringWithAnyNonAlphabeticCharactersReturnsEmptyOptional() {
        assertTrue(ps.makePalindromeFrom("1").isEmpty());
        assertTrue(ps.makePalindromeFrom("a1").isEmpty());
        assertTrue(ps.makePalindromeFrom("=").isEmpty());
        assertTrue(ps.makePalindromeFrom("=a").isEmpty());
        assertTrue(ps.makePalindromeFrom(".").isEmpty());
        assertTrue(ps.makePalindromeFrom("a.").isEmpty());
    }

    @Test
    void makePalindromeFromStringWithMultipleUnpairedCharactersReturnsEmptyOptional() {
        assertTrue(ps.makePalindromeFrom("abcc").isEmpty());
        assertTrue(ps.makePalindromeFrom("aaaabccccd").isEmpty());
        assertTrue(ps.makePalindromeFrom("aaaabbbbbccccddd").isEmpty());
    }

    @Test
    void makePalindromeFromStringWithOneUnpairedCharacterReturnsPalindrome() {
        assertEquals("aba", ps.makePalindromeFrom("aab").get());
        assertEquals("ababa", ps.makePalindromeFrom("abbaa").get());
        assertEquals("aabbccbccbbaa", ps.makePalindromeFrom("aaabbbbccccba").get());
        assertEquals("a", ps.makePalindromeFrom("a").get());
    }

    @Test
    void makePalindromeFromSingleCharacterStringReturnsSingleCharacter() {
        assertEquals("a", ps.makePalindromeFrom("a").get());
    }

    @Test
    void makePalindromeFromStringWithOnePairedCharacterReturnsPalindrome() {
        assertEquals("aa", ps.makePalindromeFrom("aa").get());
        assertEquals("aaaa", ps.makePalindromeFrom("aaaa").get());
    }

    @Test
    void makePalindromFromStringWithMultiplePairedCharactersReturnsPalindrome() {
        assertEquals("abccba", ps.makePalindromeFrom("ababcc").get());
        assertEquals("aabccccccbaa", ps.makePalindromeFrom("aaaabbcccccc").get());
        assertEquals("aaaaaabbaaaaaa", ps.makePalindromeFrom("aaaaaaaaaaaabb").get());
    }
}
