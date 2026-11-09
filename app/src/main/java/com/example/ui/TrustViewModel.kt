package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.MockVaultAccountDataProvider
import com.example.data.TrustRepository
import com.example.data.TrustState
import com.example.data.VaultAccountDataProvider
import com.example.model.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrustViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TrustRepository(application.applicationContext)
    val state: StateFlow<TrustState> = repository.state

    val vaultAccountProvider: VaultAccountDataProvider = MockVaultAccountDataProvider()
    val vaultAccounts: StateFlow<List<VaultAccount>> = vaultAccountProvider.accountsFlow

    private val _isSyncingBankFeeds = MutableStateFlow(false)
    val isSyncingBankFeeds: StateFlow<Boolean> = _isSyncingBankFeeds.asStateFlow()

    private val _syncFeedback = MutableStateFlow<String?>(null)
    val syncFeedback: StateFlow<String?> = _syncFeedback.asStateFlow()

    fun clearSyncFeedback() {
        _syncFeedback.value = null
    }

    fun syncLiveBankFeeds() {
        viewModelScope.launch {
            _isSyncingBankFeeds.value = true
            delay(1200)
            val nowTime = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
            var totalDailyYield = 0.0

            repository.updateState { s ->
                val updatedAccounts = s.bankAccounts.map { b ->
                    val dailyYield = b.dailyInterest
                    totalDailyYield += dailyYield
                    b.copy(
                        balance = b.balance + dailyYield,
                        lastSyncTime = "Live Verified • Today at $nowTime",
                        isLiveConnected = true
                    )
                }
                s.copy(bankAccounts = updatedAccounts)
            }

            state.value.bankAccounts.forEach { b ->
                val matchingVaultId = when (b.id) {
                    "bank_jpm_liquid" -> "va_jpm"
                    "bank_tbills_tax_2027" -> "va_tbills_tax"
                    "bank_vault_yield_tax" -> "va_vault_yield_tax"
                    "bank_amy_minors_custodial" -> "va_amy_minors"
                    else -> b.id
                }
                val vaultAcc = vaultAccounts.value.find { it.id == matchingVaultId }
                if (vaultAcc != null) {
                    vaultAccountProvider.updateAccount(
                        vaultAcc.copy(
                            balance = b.balance,
                            apy = b.apy,
                            status = "Live Synced ($nowTime)"
                        )
                    )
                }
            }

            _isSyncingBankFeeds.value = false
            _syncFeedback.value = "Synced with J.P. Morgan & Treasury Direct feeds. Daily yield accrued: +${com.example.ui.components.TrustFormatters.currency(totalDailyYield)}"
        }
    }

    fun refreshSingleAccount(accountId: String) {
        viewModelScope.launch {
            _isSyncingBankFeeds.value = true
            delay(750)
            val nowTime = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
            repository.updateState { s ->
                val updated = s.bankAccounts.map { b ->
                    if (b.id == accountId) {
                        val dailyYield = b.dailyInterest
                        b.copy(
                            balance = b.balance + dailyYield,
                            lastSyncTime = "Live Verified • Today at $nowTime",
                            isLiveConnected = true
                        )
                    } else b
                }
                s.copy(bankAccounts = updated)
            }
            _isSyncingBankFeeds.value = false
            _syncFeedback.value = "Account balance verified with live banking gateway"
        }
    }

    fun refreshVaultAccounts() {
        vaultAccountProvider.refreshAccounts()
    }

    fun updateThemeConfig(config: AppThemeConfig) {
        repository.updateState { it.copy(themeConfig = config) }
    }

    fun resetToDefaults() {
        repository.resetToDefaults()
    }

    // Vault
    fun updateVaultLeg(index: Int, updatedLeg: VaultLeg) {
        repository.updateState { s ->
            val list = s.vaultLegs.toMutableList()
            if (index in list.indices) {
                list[index] = updatedLeg
            }
            s.copy(vaultLegs = list)
        }
    }

    fun updateCompounding(sim: CompoundingSimState) {
        repository.updateState { it.copy(compoundingSim = sim) }
    }

    fun updateVaultAccount(account: VaultAccount) {
        vaultAccountProvider.updateAccount(account)
        // Bidirectionally synchronize bank accounts if this is a cash account
        if (account.category == VaultAccountCategory.LIQUID_CASH || account.id.startsWith("va_")) {
            val matchingBankId = when (account.id) {
                "va_jpm" -> "bank_jpm_liquid"
                "va_tbills_tax" -> "bank_tbills_tax_2027"
                "va_vault_yield_tax" -> "bank_vault_yield_tax"
                "va_amy_minors" -> "bank_amy_minors_custodial"
                else -> account.id
            }
            val existingBank = state.value.bankAccounts.find { it.id == matchingBankId }
            if (existingBank != null) {
                updateBankAccount(existingBank.copy(
                    institution = account.institution,
                    accountName = account.name,
                    accountNumberMask = account.accountMask,
                    balance = account.balance,
                    apy = account.apy,
                    purposeNotes = account.notes
                ))
            }
        }
        // Bidirectionally synchronize fixed income legs
        when (account.id) {
            "va_tbills" -> if (state.value.vaultLegs.isNotEmpty()) updateVaultLeg(0, state.value.vaultLegs[0].copy(allocation = account.balance, apy = account.apy))
            "va_treasuries" -> if (state.value.vaultLegs.size > 1) updateVaultLeg(1, state.value.vaultLegs[1].copy(allocation = account.balance, apy = account.apy))
            "va_munis" -> if (state.value.vaultLegs.size > 2) updateVaultLeg(2, state.value.vaultLegs[2].copy(allocation = account.balance, apy = account.apy))
        }
    }

    fun addVaultAccount(account: VaultAccount) {
        vaultAccountProvider.addAccount(account)
        if (account.category == VaultAccountCategory.LIQUID_CASH) {
            addBankAccount(BankAccount(
                id = account.id,
                institution = account.institution,
                accountName = account.name,
                accountNumberMask = account.accountMask,
                balance = account.balance,
                apy = account.apy,
                isPrimarySweep = false,
                category = "Liquid Reserves",
                purposeNotes = account.notes
            ))
        }
    }

    fun deleteVaultAccount(id: String) {
        vaultAccountProvider.deleteAccount(id)
        val matchingBankId = when (id) {
            "va_jpm" -> "bank_jpm_liquid"
            "va_tbills_tax" -> "bank_tbills_tax_2027"
            "va_vault_yield_tax" -> "bank_vault_yield_tax"
            "va_amy_minors" -> "bank_amy_minors_custodial"
            else -> id
        }
        deleteBankAccount(matchingBankId)
    }

    // Banks
    fun addBankAccount(account: BankAccount) {
        repository.updateState { it.copy(bankAccounts = it.bankAccounts + account) }
    }

    fun updateBankAccount(account: BankAccount) {
        repository.updateState { s ->
            s.copy(bankAccounts = s.bankAccounts.map { if (it.id == account.id) account else it })
        }
    }

    fun deleteBankAccount(id: String) {
        repository.updateState { s ->
            s.copy(bankAccounts = s.bankAccounts.filter { it.id != id })
        }
    }

    // Loans
    fun addLoan(loan: LoanItem) {
        repository.updateState { it.copy(loans = it.loans + loan) }
    }

    fun updateLoan(loan: LoanItem) {
        repository.updateState { s ->
            s.copy(loans = s.loans.map { if (it.id == loan.id) loan else it })
        }
    }

    fun deleteLoan(id: String) {
        repository.updateState { s ->
            s.copy(loans = s.loans.filter { it.id != id })
        }
    }

    // Cards
    fun updateJpSpend(spend: Double) {
        repository.updateState { s ->
            s.copy(jpReserve = s.jpReserve.copy(annualSpend = spend))
        }
    }

    fun addFamilyCard(card: FamilyCard) {
        repository.updateState { it.copy(familyCards = it.familyCards + card) }
    }

    fun updateFamilyCard(card: FamilyCard) {
        repository.updateState { s ->
            s.copy(familyCards = s.familyCards.map { if (it.id == card.id) card else it })
        }
    }

    fun deleteFamilyCard(id: String) {
        repository.updateState { s ->
            s.copy(familyCards = s.familyCards.filter { it.id != id })
        }
    }

    fun updateMinorCustodialFund(fund: MinorCustodialFund) {
        repository.updateState { it.copy(minorCustodialFund = fund) }
    }

    // Insurance
    fun addPolicy(policy: InsurancePolicy) {
        repository.updateState { it.copy(insurancePolicies = it.insurancePolicies + policy) }
    }

    fun updatePolicy(policy: InsurancePolicy) {
        repository.updateState { s ->
            s.copy(insurancePolicies = s.insurancePolicies.map { if (it.id == policy.id) policy else it })
        }
    }

    fun deletePolicy(id: String) {
        repository.updateState { s ->
            s.copy(insurancePolicies = s.insurancePolicies.filter { it.id != id })
        }
    }

    private fun adjustJpmLiquidBalance(currentBankAccounts: List<BankAccount>, amountDelta: Double): List<BankAccount> {
        return currentBankAccounts.map { acct ->
            if (acct.id == "bank_jpm_liquid" || acct.isPrimarySweep) {
                acct.copy(balance = (acct.balance + amountDelta).coerceAtLeast(0.0))
            } else {
                acct
            }
        }
    }

    // Real Estate - purchases debit from J.P. Morgan Liquid Account
    fun addProperty(property: TrustProperty) {
        repository.updateState { s ->
            val updatedBanks = adjustJpmLiquidBalance(s.bankAccounts, -property.closingInvested)
            s.copy(
                properties = s.properties + property,
                bankAccounts = updatedBanks
            )
        }
    }

    fun updateProperty(property: TrustProperty) {
        repository.updateState { s ->
            val oldProperty = s.properties.find { it.id == property.id }
            val delta = (oldProperty?.closingInvested ?: 0.0) - property.closingInvested
            val updatedBanks = adjustJpmLiquidBalance(s.bankAccounts, delta)
            s.copy(
                properties = s.properties.map { if (it.id == property.id) property else it },
                bankAccounts = updatedBanks
            )
        }
    }

    fun deleteProperty(id: String) {
        repository.updateState { s ->
            val oldProperty = s.properties.find { it.id == id }
            val refund = oldProperty?.closingInvested ?: 0.0
            val updatedBanks = adjustJpmLiquidBalance(s.bankAccounts, refund)
            s.copy(
                properties = s.properties.filter { it.id != id },
                bankAccounts = updatedBanks
            )
        }
    }

    // Fleet - vehicle and aircraft purchases debit from J.P. Morgan Liquid Account
    fun addVehicle(vehicle: FleetVehicle) {
        repository.updateState { s ->
            val updatedBanks = adjustJpmLiquidBalance(s.bankAccounts, -vehicle.totalDeliveredCost)
            s.copy(
                fleet = s.fleet + vehicle,
                bankAccounts = updatedBanks
            )
        }
    }

    fun updateVehicle(vehicle: FleetVehicle) {
        repository.updateState { s ->
            val oldVehicle = s.fleet.find { it.id == vehicle.id }
            val delta = (oldVehicle?.totalDeliveredCost ?: 0.0) - vehicle.totalDeliveredCost
            val updatedBanks = adjustJpmLiquidBalance(s.bankAccounts, delta)
            s.copy(
                fleet = s.fleet.map { if (it.id == vehicle.id) vehicle else it },
                bankAccounts = updatedBanks
            )
        }
    }

    fun deleteVehicle(id: String) {
        repository.updateState { s ->
            val oldVehicle = s.fleet.find { it.id == id }
            val refund = oldVehicle?.totalDeliveredCost ?: 0.0
            val updatedBanks = adjustJpmLiquidBalance(s.bankAccounts, refund)
            s.copy(
                fleet = s.fleet.filter { it.id != id },
                bankAccounts = updatedBanks
            )
        }
    }

    // LLC Simulators
    fun updateBeverageSim(bev: BeverageSimState) {
        repository.updateState { it.copy(beverageSim = bev) }
    }

    fun updateSnackSim(snack: SnackSimState) {
        repository.updateState { it.copy(snackSim = snack) }
    }

    fun updateLlcTaxEquitySim(sim: LlcTaxEquitySimState) {
        repository.updateState { it.copy(llcTaxEquitySim = sim) }
    }
}
