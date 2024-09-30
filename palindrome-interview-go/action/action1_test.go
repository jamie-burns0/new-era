package action_test

import (
	"testing"

	"jamieburns.me/palidrome-interview/action"
)

func TestIsAPalindromeCandidate( t *testing.T ) {
	
	if action.IsAPalindromeCandidate( "abcdef" ) != true {
		t.Error( "expected true." )
	}

	if action.IsAPalindromeCandidate( "ab4cdef" ) != false {
		t.Error( "expected false." )
	}

	if action.IsAPalindromeCandidate( "" ) != true {
		t.Error( "expected false." )
	}
}