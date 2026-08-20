package no.roedt.qomon.externalModel

import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class RolesResponse(val status: Status, val data: Roles)

@RegisterForReflection
data class Roles(val count: Int, val roles: List<Role>) {
    fun organisator() = roles.single { it.name == "Organisator" }
}

// TODO: har order eigentleg strengare type her?
/*
0 Aksjonsleder
1 superadmin
2 administrator
3 organisator
4 aktivist
 */
@RegisterForReflection
data class Role(
    val id: Int,
    val name: String?,
    val order: Int,
    val type: RoleType,
)

enum class RoleType {
    manager, admin, superadmin, user, custom
}

enum class Status {
    success
}

@RegisterForReflection
data class RolePatchRequest(
    @JsonProperty("data") val data: Data
) {
    data class Data(
        @JsonProperty("role_id") val roleId: Int,
        @JsonProperty("user_id") val userId: Int
    )
}

@RegisterForReflection
data class RolePatchResponse(
    val data: String,
    val status: Status,
)