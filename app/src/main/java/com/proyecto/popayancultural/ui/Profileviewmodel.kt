package com.proyecto.popayancultural.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.proyecto.popayancultural.data.RetrofitClient
import com.proyecto.popayancultural.data.TokenManager
import com.proyecto.popayancultural.data.models.UpdateSettingsRequest
import com.proyecto.popayancultural.data.models.UserFull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

// ─────────────────────────────────────────────────────────────
//  MODELOS UI
// ─────────────────────────────────────────────────────────────

data class SocialMedia(
    val instagram: String = "",
    val facebook : String = "",
    val whatsapp : String = "",
    val twitter  : String = ""
)

data class UserSettings(
    val publicProfile      : Boolean = true,
    val emailNotifications : Boolean = true,
    val nearbyEventsNotify : Boolean = true,
    val language           : String  = "es",
    val visualMode         : String  = "dark"
)

data class UserProfile(
    val id            : Int          = 0,
    val name          : String       = "",
    val username      : String       = "",
    val email         : String       = "",
    val phone         : String       = "",
    val city          : String       = "Popayán",
    val neighborhood  : String       = "",
    val bio           : String       = "",
    val website       : String       = "",
    val profilePicture: String       = "",
    val coverPicture  : String       = "",
    val socialMedia   : SocialMedia  = SocialMedia(),
    val userType      : String       = "visitor",
    val isVerified    : Boolean      = false,
    val settings      : UserSettings = UserSettings()
)

enum class EffectiveRole { VISITOR, ARTIST, CULTURAL_MANAGER, EDUCATOR, ADMIN }

fun UserProfile.effectiveRole(): EffectiveRole = when {
    userType == "admin"                                   -> EffectiveRole.ADMIN
    userType == "artist"           && isVerified          -> EffectiveRole.ARTIST
    userType == "cultural_manager" && isVerified          -> EffectiveRole.CULTURAL_MANAGER
    userType == "educator"         && isVerified          -> EffectiveRole.EDUCATOR
    else                                                  -> EffectiveRole.VISITOR
}

enum class SettingsTab { IDENTITY, CONTACT, PRIVACY, PREFERENCES }

data class ProfileUiState(
    val profile           : UserProfile  = UserProfile(),
    val isLoading         : Boolean      = false,
    val isSaving          : Boolean      = false,
    val saveSuccess       : Boolean      = false,
    val errorMessage      : String?      = null,
    val pendingAvatarUri  : Uri?         = null,
    val pendingCoverUri   : Uri?         = null,
    val activeSettingsTab : SettingsTab  = SettingsTab.IDENTITY
)

// ─────────────────────────────────────────────────────────────
//  VIEW MODEL
// ─────────────────────────────────────────────────────────────

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application.applicationContext)
    private val gson         = Gson()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // ── Carga del perfil ─────────────────────────────────────
    fun loadProfile() {
        tokenManager.getUser()?.let { localUser ->
            _uiState.update { it.copy(profile = localUser.toUserProfile()) }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = RetrofitClient.apiService.getProfile()
                if (response.isSuccessful && response.body()?.status == "success") {
                    val fresh = response.body()!!.user!!
                    tokenManager.saveUser(fresh.toAuthUser())
                    _uiState.update { it.copy(profile = fresh.toUserProfile(), isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // ── Guardar perfil ────────────────────────────────────────
    fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, saveSuccess = false) }
            try {
                val profile = _uiState.value.profile
                val ctx     = getApplication<Application>().applicationContext

                fun String.toText() = toRequestBody("text/plain".toMediaTypeOrNull())

                val socialJson = gson.toJson(mapOf(
                    "instagram" to profile.socialMedia.instagram,
                    "facebook"  to profile.socialMedia.facebook,
                    "whatsapp"  to profile.socialMedia.whatsapp,
                    "twitter"   to profile.socialMedia.twitter
                ))

                val avatarPart = _uiState.value.pendingAvatarUri?.let { uri ->
                    uriToMultipart(ctx, uri, "profile_picture")
                }
                val coverPart = _uiState.value.pendingCoverUri?.let { uri ->
                    uriToMultipart(ctx, uri, "cover_picture")
                }

                val profileResult = RetrofitClient.apiService.updateProfile(
                    name           = profile.name.toText(),
                    bio            = profile.bio.toText(),
                    phone          = profile.phone.toText(),
                    city           = profile.city.toText(),
                    neighborhood   = profile.neighborhood.toText(),
                    website        = profile.website.toText(),
                    socialMedia    = socialJson.toText(),
                    profilePicture = avatarPart,
                    coverPicture   = coverPart
                )

                RetrofitClient.apiService.updateSettings(
                    UpdateSettingsRequest(
                        publicProfile      = profile.settings.publicProfile,
                        emailNotifications = profile.settings.emailNotifications,
                        nearbyEventsNotify = profile.settings.nearbyEventsNotify,
                        language           = profile.settings.language,
                        visualMode         = profile.settings.visualMode
                    )
                )

                if (profileResult.isSuccessful && profileResult.body()?.status == "success") {
                    val fresh = profileResult.body()!!.user!!
                    tokenManager.saveUser(fresh.toAuthUser())
                    _uiState.update {
                        it.copy(
                            profile          = fresh.toUserProfile(),
                            isSaving         = false,
                            saveSuccess      = true,
                            pendingAvatarUri = null,
                            pendingCoverUri  = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = "Error al guardar cambios")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = "Error de conexión: ${e.message}")
                }
            }
        }
    }

    // ── Edición campos — Identidad ────────────────────────────
    fun onNameChange(v: String)         = _uiState.update { it.copy(profile = it.profile.copy(name = v)) }
    fun onBioChange(v: String)          = _uiState.update { it.copy(profile = it.profile.copy(bio = v)) }
    fun onPhoneChange(v: String)        = _uiState.update { it.copy(profile = it.profile.copy(phone = v)) }
    fun onCityChange(v: String)         = _uiState.update { it.copy(profile = it.profile.copy(city = v)) }
    fun onNeighborhoodChange(v: String) = _uiState.update { it.copy(profile = it.profile.copy(neighborhood = v)) }
    fun onWebsiteChange(v: String)      = _uiState.update { it.copy(profile = it.profile.copy(website = v)) }

    // ── Edición campos — Contacto ─────────────────────────────
    fun onInstagramChange(v: String) = _uiState.update { it.copy(profile = it.profile.copy(socialMedia = it.profile.socialMedia.copy(instagram = v))) }
    fun onFacebookChange(v: String)  = _uiState.update { it.copy(profile = it.profile.copy(socialMedia = it.profile.socialMedia.copy(facebook = v))) }
    fun onWhatsappChange(v: String)  = _uiState.update { it.copy(profile = it.profile.copy(socialMedia = it.profile.socialMedia.copy(whatsapp = v))) }
    fun onTwitterChange(v: String)   = _uiState.update { it.copy(profile = it.profile.copy(socialMedia = it.profile.socialMedia.copy(twitter = v))) }

    // ── Edición campos — Privacidad ───────────────────────────
    fun onPublicProfileChange(v: Boolean)      = _uiState.update { it.copy(profile = it.profile.copy(settings = it.profile.settings.copy(publicProfile = v))) }
    fun onEmailNotificationsChange(v: Boolean) = _uiState.update { it.copy(profile = it.profile.copy(settings = it.profile.settings.copy(emailNotifications = v))) }
    fun onNearbyEventsChange(v: Boolean)       = _uiState.update { it.copy(profile = it.profile.copy(settings = it.profile.settings.copy(nearbyEventsNotify = v))) }

    // ── Edición campos — Preferencias ─────────────────────────
    fun onLanguageChange(v: String)   = _uiState.update { it.copy(profile = it.profile.copy(settings = it.profile.settings.copy(language = v))) }
    fun onVisualModeChange(v: String) = _uiState.update { it.copy(profile = it.profile.copy(settings = it.profile.settings.copy(visualMode = v))) }

    // ── Imágenes ──────────────────────────────────────────────
    fun onAvatarSelected(uri: Uri) = _uiState.update { it.copy(pendingAvatarUri = uri) }
    fun onCoverSelected(uri: Uri)  = _uiState.update { it.copy(pendingCoverUri = uri) }

    // ── Navegación de tabs ────────────────────────────────────
    fun onTabSelected(tab: SettingsTab) = _uiState.update { it.copy(activeSettingsTab = tab) }

    // ── Feedback ──────────────────────────────────────────────
    fun clearSuccess() = _uiState.update { it.copy(saveSuccess = false) }
    fun clearError()   = _uiState.update { it.copy(errorMessage = null) }

    // ── Guardados ─────────────────────────────────────────────
    private val _savedItems = MutableStateFlow<List<com.proyecto.popayancultural.data.models.SavedItem>>(emptyList())
    val savedItems: StateFlow<List<com.proyecto.popayancultural.data.models.SavedItem>> = _savedItems.asStateFlow()

    private val _savedLoading = MutableStateFlow(false)
    val savedLoading: StateFlow<Boolean> = _savedLoading.asStateFlow()

    fun loadSavedItems() {
        if (_savedLoading.value) return
        viewModelScope.launch {
            _savedLoading.update { true }
            try {
                val response = RetrofitClient.apiService.getSavedItems()
                if (response.isSuccessful && response.body()?.status == "success") {
                    _savedItems.update { response.body()!!.data }
                }
            } catch (e: Exception) {
                // silencioso — la UI ya muestra empty state
            } finally {
                _savedLoading.update { false }
            }
        }
    }

    // ── Helper: URI → MultipartBody.Part ─────────────────────
    private fun uriToMultipart(ctx: Context, uri: Uri, fieldName: String): MultipartBody.Part? {
        return try {
            val inputStream = ctx.contentResolver.openInputStream(uri) ?: return null
            val file = File(ctx.cacheDir, "${fieldName}_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out -> inputStream.copyTo(out) }
            val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(fieldName, file.name, requestBody)
        } catch (e: Exception) { null }
    }
}

// ─────────────────────────────────────────────────────────────
//  MAPPERS
// ─────────────────────────────────────────────────────────────

fun UserFull.toUserProfile() = UserProfile(
    id             = id,
    name           = name           ?: "",
    username       = username       ?: "",
    email          = email          ?: "",
    phone          = phone          ?: "",
    city           = city           ?: "Popayán",
    neighborhood   = neighborhood   ?: "",
    bio            = bio            ?: "",
    website        = website        ?: "",
    profilePicture = profilePicture ?: "",
    coverPicture   = coverPicture   ?: "",
    userType       = userType,
    isVerified     = isVerified,
    socialMedia    = SocialMedia(
        instagram = socialMedia?.instagram ?: "",
        facebook  = socialMedia?.facebook  ?: "",
        whatsapp  = socialMedia?.whatsapp  ?: "",
        twitter   = socialMedia?.twitter   ?: ""
    ),
    settings = UserSettings(
        publicProfile      = settings?.publicProfile      ?: true,
        emailNotifications = settings?.emailNotifications ?: true,
        nearbyEventsNotify = settings?.nearbyEventsNotify ?: true,
        language           = settings?.language           ?: "es",
        visualMode         = settings?.visualMode         ?: "dark"
    )
)

fun UserFull.toAuthUser() = com.proyecto.popayancultural.data.models.User(
    id             = id,
    name           = name           ?: "",
    email          = email          ?: "",
    username       = username       ?: "",
    user_type      = userType,
    profilePicture = profilePicture,
    isVerified     = isVerified
)

fun com.proyecto.popayancultural.data.models.User.toUserProfile() = UserProfile(
    id             = id,
    name           = name           ?: "",
    username       = username       ?: "",
    email          = email          ?: "",
    userType       = user_type      ?: "visitor",
    isVerified     = isVerified,
    profilePicture = profilePicture ?: ""
)