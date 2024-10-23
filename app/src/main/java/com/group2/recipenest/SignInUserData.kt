package com.group2.recipenest

data class SignInUserData(
    var UserUID: String,
    var UserDocId: String,
    var IsFingerprintAuthEnabled: Boolean
)

var userSignInData: SignInUserData = SignInUserData("","",false)
