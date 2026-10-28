package com.eversince.data.model

import java.time.LocalDate

data class Relationship(
    val person1: String = "Madhu",
    val person2: String = "Partner",
    // యాప్ ఓపెన్ చేయగానే కౌంటర్ పనిచేస్తుందో లేదో చూడటానికి ఒక పాత తేదీ ఇక్కడ ఇచ్చాను.
    // మీకు నచ్చిన తేదీని ఇక్కడ పెట్టుకోవచ్చు.
    val startDate: LocalDate = LocalDate.of(2023, 1, 1) 
)
