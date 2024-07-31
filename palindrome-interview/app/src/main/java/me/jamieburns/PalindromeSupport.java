package me.jamieburns;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.function.Predicate;

public class PalindromeSupport {

    private static Predicate<String> INVALID_CHARACTERS_PREDICATE = Pattern.compile("[^a-zA-Z]").asPredicate();

    public Optional<String> makePalindromeFrom( String data ) {

        if( isNotPalindromeCandidate( data ) ) {
            return Optional.empty();
        }

        var frequencyOfCharactersMap = frequencyOfCharactersMap( data );

        if( isNotPalindromeCandidate( frequencyOfCharactersMap ) )
        {
            return Optional.empty();
        }

        var palindrome = frequencyOfCharactersMapToPalindrome( frequencyOfCharactersMap );

        return Optional.of( String.valueOf( palindrome ));
    }

    public Map<Character, Long> frequencyOfCharactersMap( String data )
    {
        return data.chars()
                   .mapToObj(i -> (char) i)
                   .collect(
                       Collectors.groupingBy(
                           Function.identity(),
                           Collectors.counting()
                       )
                   );
    }

    public boolean isNotPalindromeCandidate( Map<Character, Long> frequencyOfCharactersMap )
    {
        return frequencyOfCharactersMap.entrySet().stream()
                .filter( e -> e.getValue() % 2 == 1 )
                .count() > 1;
    }

    public boolean isNotPalindromeCandidate( String data )
    {
        if( data == null || data.length() == 0 )
        {
            return true;
        }

        return INVALID_CHARACTERS_PREDICATE.test( data );        
    }


    public String frequencyOfCharactersMapToPalindrome( Map<Character, Long> frequencyOfCharactersMap )
    {
        var palindromeLength = frequencyOfCharactersMap.values().stream().mapToInt(Long::intValue).sum();

        var palindrome = new char[palindromeLength];

        var left = 0;
        var right = palindromeLength - 1;
    
        for( var entry : frequencyOfCharactersMap.entrySet() )
        {
            var character = entry.getKey();
            var frequency = entry.getValue();

            if( frequency % 2 == 1 )
            {
                palindrome[ palindromeLength >>> 1] = character;
                frequency--;
            }

            while( frequency > 0 )
            {
                frequency -= 2;
                palindrome[left] = character;
                palindrome[right] = character;
                left++;
                right--;
            }
        }

        return String.valueOf( palindrome );
    }
}