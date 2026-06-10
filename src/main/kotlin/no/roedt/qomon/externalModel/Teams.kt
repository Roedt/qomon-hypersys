package no.roedt.qomon.externalModel

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class TeamsResponse(val status: Status, val data: Data)

@RegisterForReflection
data class Data(val teams: List<Team>)

data class TeamUser(val id: Int)

@RegisterForReflection
data class Team(
    val id: Int,
    val name: String,
    val description: String = "",
    val private: Boolean = false,
    val hide_users: Boolean = true,
    val users: List<TeamUser> = listOf(),
    val leaders: List<TeamUser> = listOf(),
//    val address: TeamAddress? = null
)
data class TeamAddress(
    val street: String? = null,
)

data class GetTeamResponse(val status: Status, val data: GetTeamData)

data class GetTeamData(val team: Team)


@RegisterForReflection
data class CreateTeamRequest(val data: CreateTeamResponseData)

data class CreateTeamResponse(val data: CreateTeamResponseData)

data class CreateTeamResponseData(val team: Team)


data class PatchTeamRequest(val data: PatchTeamData)

data class PatchTeamData(val team: Team)