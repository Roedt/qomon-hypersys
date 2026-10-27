package no.roedt.hypersys.externalModel

import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class Bruker(
    @JsonProperty("pk") val id: Int,
    @JsonProperty("memberships") val memberships: List<Membership>
)