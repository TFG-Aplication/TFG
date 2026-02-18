/**
val calendarsList: StateFlow<List<CalendarModel>> = (getCalendarsUseCase(userId) ?: flowOf(emptyList()))
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

 dentro del init:
viewModelScope.launch {
calendarsList.collect { list ->
if (list.isNotEmpty() && _uiState.value.calendar == null) {
onCalendarChanged(list.first())
}
}
}

fun onCalendarChanged(newCalendar: CalendarModel) {
_uiState.update { it.copy(calendar = newCalendar) }
}

fun onNameChanged(newName: String) {
_uiState.update { it.copy(name = newName) }
}
 **/