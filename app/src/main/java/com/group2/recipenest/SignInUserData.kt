package com.group2.recipenest

data class SignInUserData(
    var UserUID: String,
    var UserDocId: String,
    var ShowAuthFirstTime: Boolean
)

var userSignInData: SignInUserData = SignInUserData("","",true)
