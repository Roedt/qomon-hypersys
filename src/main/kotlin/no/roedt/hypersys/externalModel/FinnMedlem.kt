package no.roedt.hypersys.externalModel

import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class FinnMedlem(
    @JsonProperty("pk") val id: Int // HypersysID
)