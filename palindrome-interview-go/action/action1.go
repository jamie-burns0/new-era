package action

import "regexp"

func IsAPalindromeCandidate( s string ) bool {

	matched, _ := regexp.Match( `^[a-z]*$`, []byte(s) )

	return matched
}