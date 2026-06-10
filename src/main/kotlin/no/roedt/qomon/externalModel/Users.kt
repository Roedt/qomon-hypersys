package no.roedt.qomon.externalModel

data class GetUsersResponse(val status: Status, val data: GetUsersData)

data class GetUsersData(val count: Int, val users: List<User>)

data class User(val id: Int, val mail: String, val firstname: String, val surname: String, val postalcode: String? = null)