package no.roedt.qomon

import jakarta.enterprise.context.Dependent
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.HeaderParam
import jakarta.ws.rs.PATCH
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import no.roedt.qomon.externalModel.CreateTeamRequest
import no.roedt.qomon.externalModel.CreateTeamResponse
import no.roedt.qomon.externalModel.GetTeamResponse
import no.roedt.qomon.externalModel.GetUsersResponse
import no.roedt.qomon.externalModel.PatchTeamRequest
import no.roedt.qomon.externalModel.RolesResponse
import no.roedt.qomon.externalModel.Team
import no.roedt.qomon.externalModel.TeamsResponse
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@Dependent
@RegisterRestClient
interface QomonRestClient {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/roles")
    fun roller(
        @HeaderParam("Authorization") authorization: String,
    ) : RolesResponse

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/teams")
    fun teams(@HeaderParam("Authorization") authorization: String) : TeamsResponse

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/teams/{id}")
    fun team(@HeaderParam("Authorization") authorization: String, @PathParam("id") id: Int) : GetTeamResponse

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/teams")
    fun opprettTeam(@HeaderParam("Authorization") authorization: String,  request: CreateTeamRequest): CreateTeamResponse

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/users")
    fun getUsers(@HeaderParam("Authorization") authorization: String) : GetUsersResponse

    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/teams")
    fun oppdaterTeam(@HeaderParam("Authorization") authorization: String, oppdatert: PatchTeamRequest): CreateTeamResponse
}