package com.asistente.core.data.seeders.category

import com.asistente.core.domain.models.Category

object MockDataCategory {
        fun getPredefinedCategories(calendarId: String): List<Category> {
            return listOf(
                Category(
                    name = "Trabajo",
                    parentCalendarId = calendarId,
                    color = "#ff5757"
                ),
                Category(
                    name = "Personal",
                    parentCalendarId = calendarId,
                    color = "#ffde59"
                ),
                Category(
                    name = "Evento",
                    parentCalendarId = calendarId,
                    color = "#5ce1e6"
                ),
                Category(
                    name = "Salud",
                    parentCalendarId = calendarId,
                    color = "#c1ff72"
                ),
                Category(
                    name = "Viajes",
                    parentCalendarId = calendarId,
                    color = "#e2a9f1"
                )
            )
    }
}