package no.roedt.hypersys.externalModel.membership

import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class Membership(
    @JsonProperty("member_id") val member_id: Int,
    @JsonProperty("email") val email: String?,
    @JsonProperty("organisation_id") val organisation_id: Int,
    @JsonProperty("name") val name: String,
)