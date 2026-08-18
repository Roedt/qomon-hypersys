package no.roedt.qomon.externalModel

data class GetUsersResponse(val status: Status, val data: GetUsersData)

data class GetUsersData(val count: Int, val users: List<User>)

data class User(val id: Int, val mail: String?, val firstname: String, val surname: String, val postalcode: String? = null, val role_data: RoleData)

data class GetUserResponse(val data: Data, val status: Status) {
    data class Data(val user: User)
}

// TODO: har nokre av desse felta eigentleg strengare type?
data class RoleData(
    val id: Int,
    val name: String?,
    val order: Int,
)