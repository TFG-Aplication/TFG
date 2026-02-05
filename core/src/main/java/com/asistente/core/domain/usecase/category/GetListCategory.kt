package com.asistente.core.domain.usecase.category

import com.asistente.core.domain.models.Category
import com.asistente.core.domain.ropositories.interfaz.CategoryRepositoryInterface
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Obtiene una lista de todos los Categorias de un calendario

 */
class GetListCategory @Inject constructor(
    private val repository: CategoryRepositoryInterface,
){
     operator fun invoke(calendarId: String): Flow<List<Category>>? {
        return repository.getAllCategoryByCalendarId(calendarId)

    }



}