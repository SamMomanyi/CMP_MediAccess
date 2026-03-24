package org.sammomanyi.mediaccess.features.pharmacy.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.features.cover.data.CoverRepository
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverLinkRequest
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverStatus
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GetCurrentUserUseCase
import org.sammomanyi.mediaccess.features.pharmacy.data.ExpenditureRepository
import org.sammomanyi.mediaccess.features.pharmacy.domain.model.Expenditure

class ExpenditureHistoryViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val expenditureRepository: ExpenditureRepository,
    private val coverRepository: CoverRepository
) : ViewModel() {

    private val _expenditures = MutableStateFlow<List<Expenditure>>(emptyList())
    val expenditures: StateFlow<List<Expenditure>> = _expenditures.asStateFlow()

    private val _coverInfo = MutableStateFlow<CoverLinkRequest?>(null)
    val coverInfo: StateFlow<CoverLinkRequest?> = _coverInfo.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Get current user
            val user = getCurrentUserUseCase().firstOrNull() ?: run {
                println("🔴 EXPENDITURE: No current user found")
                return@launch
            }

            println("💰 EXPENDITURE: Loading data for user: ${user.email}")

            // Load expenditures
            expenditureRepository.observeUserExpenditures(user.id).collect { expends ->
                println("💰 EXPENDITURE: Loaded ${expends.size} transactions")
                _expenditures.value = expends
            }
        }

        viewModelScope.launch {
            // Get current user again for cover info
            val user = getCurrentUserUseCase().firstOrNull() ?: return@launch

            // Load cover info
            coverRepository.getUserRequests(user.id).collect { requests ->
                val approvedCover = requests.firstOrNull { it.status == CoverStatus.APPROVED }
                println("💰 EXPENDITURE: Cover balance: ${approvedCover?.remainingBalance ?: 0.0}")
                _coverInfo.value = approvedCover
            }
        }
    }
}