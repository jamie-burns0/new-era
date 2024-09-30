package workflow

import "jamieburns.me/palidrome-interview/action"

func Execute( s string ) string {

	palindrome := "palindrome placeholder"

	if action.IsAPalindromeCandidate( s ) == false {
		return "Not a palindrome candidate. Contains characters other than lowercase alphabetic characters"
	}

	return palindrome
}