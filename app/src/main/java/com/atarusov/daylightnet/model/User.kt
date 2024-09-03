package com.atarusov.daylightnet.model

data class User(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val additionalInfo: String? = null,
    val avatar: String? = null
) {
    data class LoginData(
        val email: String,
        val password: String
    )

    data class RegistrationData(
        val firstName: String,
        val lastName: String,
        val email: String,
        val password: String,
        val repeatPassword: String
    )

    companion object {
        fun fromMap(map: Map<String, Any>): User {
            return User(
                uid = map["uid"] as? String ?: "wrong value",
                firstName = map["firstName"] as? String ?: "wrong value",
                lastName = map["lastName"] as? String ?: "wrong value",
                additionalInfo = map["additionalInfo"] as? String,
                avatar = map["avatar"] as? String,
            )
        }
    }
}
