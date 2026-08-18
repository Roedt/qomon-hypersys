package no.roedt.hypersys.externalModel

import io.quarkus.runtime.annotations.RegisterForReflection
import java.time.LocalDate

@RegisterForReflection
data class Verv(
    val id: Int, // Nøkkel for den spesifikke rolla
    val name: String, // Namn på personen med rolla
    val organisation_name: String, // Namnet på organisasjonen kor rolla er
    val role_type: Int, // ID på rolletypen
    val role_type_name: String, // Namnet på rolletypen
    val end_date: LocalDate?, // Når rolla er avslutta (kan vere blank, og kan avvike frå organet - t.d. om person har trukke seg i løpet av perioden)
)