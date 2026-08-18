package no.roedt.hypersys

import jakarta.enterprise.context.Dependent
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.FormParam
import jakarta.ws.rs.GET
import jakarta.ws.rs.HeaderParam
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import no.roedt.hypersys.externalModel.IsMember
import no.roedt.hypersys.externalModel.Member
import no.roedt.hypersys.externalModel.Organ
import no.roedt.hypersys.externalModel.Organisasjonsledd
import no.roedt.hypersys.externalModel.Profile
import no.roedt.hypersys.externalModel.SingleOrgan
import no.roedt.hypersys.externalModel.Verv
import no.roedt.hypersys.externalModel.membership.Membership
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import java.time.LocalDate

@Dependent
@RegisterRestClient
interface HypersysRestClient {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("api/o/token/")
    fun tokenPerson(
        @HeaderParam("Authorization") base64Credentials: String, // basic
        @FormParam("username") brukernavn: String,
        @FormParam("password") password: String,
        @FormParam("grant_type") grantType: String = "password"
    ): GyldigPersonToken

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("api/o/token/")
    fun tokenSystem(
        @HeaderParam("Authorization") base64Credentials: String,
        @FormParam("grant_type") grantType: String = "client_credentials"
    ): GyldigSystemToken

    @GET
    @Path("/old/membership/api/membership/{hypersysLokallagId}/{aar}/")
    fun hentMedlemmerILag(
        @PathParam("hypersysLokallagId") hypersysLokallagId: Int,
        @PathParam("aar") aar: Int = LocalDate.now().year,
        @HeaderParam("Authorization") token: String // Bearer
    ) : List<Membership>

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/org/api/")
    fun hentAlleLokallag(
        @HeaderParam("Authorization") token: String // Bearer
    ) : List<Organisasjonsledd>

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/org/api/{orgid}/")
    fun hentLag(
        @HeaderParam("Authorization") token: String, // Bearer,
        @PathParam("orgid") orgId: String
    ) : Map<String, Any>

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/org/api/{orgid}/organ/")
    fun hentAlleOrgan(
        @HeaderParam("Authorization") token: String, // Bearer,
        @PathParam("orgid") orgId: String
    ) : Map<String, List<Organ>>

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/org/api/{orgid}/organ/{organid}/")
    fun hentOrgan(
        @HeaderParam("Authorization") token: String, // Bearer,
        @PathParam("orgid") orgId: String,
        @PathParam("organid") organId: String
    ) : SingleOrgan

    @GET
    @Path("/membership/api/is_member/{hypersysId}/")
    fun hentPerson(
        @PathParam("hypersysId") hypersysId: Int,
        @HeaderParam("Authorization") token: String // Bearer
    ) : IsMember

    @GET
    @Path("actor/api/profile/")
    fun hentProfil(
        @HeaderParam("Authorization") token: String // Bearer
    ) : Profile

    @GET
    @Path("old/auth/api/user/{userId}/")
    fun hentBruker(
        @HeaderParam("Authorization") token: String, // Bearer
        @PathParam("userId") userId: String
    ) : HentBruker

    @GET
    @Path("old/org/api/roles-current/{orgId}/")
    fun hentVerv(
        @HeaderParam("Authorization") token: String, // Bearer
        @PathParam("orgId") orgId: String
    ) : List<Verv>
}

data class HentBruker(
    val email: String
)