package com.group2.recipenest

data class SignUpUserData(
    var firstName: String,
    var lastName: String,
    var email: String,
    var password: String,
    var username: String,
    var description: String,
    var profileimage: String
    )

var userData : SignUpUserData = SignUpUserData( "", "", "","","","","")