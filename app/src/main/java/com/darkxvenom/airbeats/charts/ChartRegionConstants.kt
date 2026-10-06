package com.darkxvenom.airbeats.charts

import androidx.datastore.preferences.core.stringPreferencesKey

val ChartRegionKey = stringPreferencesKey("airbeats_chart_region")

val ChartRegionSlugToName: Map<String, String> = mapOf(
    "system" to "System Default",
    "us" to "Global (USA)",
    "in" to "India",
    "gb" to "United Kingdom",
    "ca" to "Canada",
    "au" to "Australia",
    "jp" to "Japan",
    "kr" to "South Korea",
    "de" to "Germany",
    "fr" to "France",
    "br" to "Brazil",
    "mx" to "Mexico",
    "ru" to "Russia",
    "it" to "Italy",
    "es" to "Spain",
    "nl" to "Netherlands",
    "se" to "Sweden",
    "no" to "Norway",
    "dk" to "Denmark",
    "fi" to "Finland",
    "pl" to "Poland",
    "tr" to "Turkey",
    "za" to "South Africa",
    "ng" to "Nigeria",
    "id" to "Indonesia",
    "my" to "Malaysia",
    "ph" to "Philippines",
    "th" to "Thailand",
    "vn" to "Vietnam",
    "tw" to "Taiwan",
    "hk" to "Hong Kong",
    "sg" to "Singapore",
    "ar" to "Argentina",
    "co" to "Colombia",
    "cl" to "Chile",
    "pe" to "Peru",
    "eg" to "Egypt",
    "sa" to "Saudi Arabia",
    "ae" to "United Arab Emirates",
    "il" to "Israel"
)
