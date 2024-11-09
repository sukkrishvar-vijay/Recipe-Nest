package com.group2.recipenest

// data class to store user data when he signs in for later use
data class SignInUserData(
    var UserUID: String,
    var UserDocId: String,
    var ShowAuthFirstTime: Boolean
)

var userSignInData: SignInUserData = SignInUserData("","",true)
