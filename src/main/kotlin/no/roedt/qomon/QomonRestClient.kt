package no.roedt.qomon

import jakarta.enterprise.context.Dependent
import jakarta.ws.rs.GET
import jakarta.ws.rs.HeaderParam
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import no.roedt.qomon.externalModel.RolesResponse
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
}