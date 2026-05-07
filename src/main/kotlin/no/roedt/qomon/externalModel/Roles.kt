package no.roedt.qomon.externalModel

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class RolesResponse(val status: Status, val data: Roles)

@RegisterForReflection
data class Roles(val count: Int, val roles: List<Role>)

@RegisterForReflection
data class Role(
    val name: String?,
    val order: Int,
    val type: RoleType,
)

enum class RoleType {
    manager, admin, superadmin, user
}

enum class Status {
    success
}