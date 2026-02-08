package org.sammomanyi.mediaccess.core.presentation

import androidx.compose.runtime.Composable
import mediaccess.composeapp.generated.resources.Res
import mediaccess.composeapp.generated.resources.error_account_disabled
import mediaccess.composeapp.generated.resources.error_code_expired
import mediaccess.composeapp.generated.resources.error_code_invalid
import mediaccess.composeapp.generated.resources.error_code_used
import mediaccess.composeapp.generated.resources.error_email_exists
import mediaccess.composeapp.generated.resources.error_field_required
import mediaccess.composeapp.generated.resources.error_invalid_credentials
import mediaccess.composeapp.generated.resources.error_invalid_email
import mediaccess.composeapp.generated.resources.error_invalid_phone
import mediaccess.composeapp.generated.resources.error_no_internet
import mediaccess.composeapp.generated.resources.error_not_found
import mediaccess.composeapp.generated.resources.error_password_too_short
import mediaccess.composeapp.generated.resources.error_password_weak
import mediaccess.composeapp.generated.resources.error_request_timeout
import mediaccess.composeapp.generated.resources.error_server
import mediaccess.composeapp.generated.resources.error_session_expired
import mediaccess.composeapp.generated.resources.error_unauthorized
import mediaccess.composeapp.generated.resources.error_unknown
import mediaccess.composeapp.generated.resources.error_user_not_found
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.sammomanyi.mediaccess.core.domain.util.DataError
// Replace with your actual generated Res path

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    data class Resource(val res: StringResource) : UiText

    companion object {
        fun from(error: DataError): UiText {
            return when (error) {
                is DataError.Network -> when (error) {
                    DataError.Network.REQUEST_TIMEOUT -> Resource(Res.string.error_request_timeout)
                    DataError.Network.NO_INTERNET -> Resource(Res.string.error_no_internet)
                    DataError.Network.SERVER_ERROR -> Resource(Res.string.error_server)
                    DataError.Network.UNAUTHORIZED -> Resource(Res.string.error_unauthorized)
                    DataError.Network.NOT_FOUND -> Resource(Res.string.error_not_found)
                    else -> Resource(Res.string.error_unknown)
                }

                is DataError.Validation -> when (error) {
                    DataError.Validation.EMPTY_FIELD -> Resource(Res.string.error_field_required)
                    DataError.Validation.INVALID_EMAIL -> Resource(Res.string.error_invalid_email)
                    DataError.Validation.PASSWORD_TOO_SHORT -> Resource(Res.string.error_password_too_short)
                    DataError.Validation.PASSWORD_TOO_WEAK -> Resource(Res.string.error_password_weak)
                    DataError.Validation.INVALID_PHONE_NUMBER -> Resource(Res.string.error_invalid_phone)
                    DataError.Validation.INVALID_VISIT_CODE -> Resource(Res.string.error_code_invalid)
                    DataError.Validation.VISIT_CODE_EXPIRED -> Resource(Res.string.error_code_expired)
                    DataError.Validation.VISIT_CODE_USED -> Resource(Res.string.error_code_used)
                    else -> Resource(Res.string.error_unknown)
                }

                is DataError.Auth -> when (error) {
                    DataError.Auth.INVALID_CREDENTIALS -> Resource(Res.string.error_invalid_credentials)
                    DataError.Auth.USER_NOT_FOUND -> Resource(Res.string.error_user_not_found)
                    DataError.Auth.EMAIL_ALREADY_EXISTS -> Resource(Res.string.error_email_exists)
                    DataError.Auth.ACCOUNT_DISABLED -> Resource(Res.string.error_account_disabled)
                    DataError.Auth.SESSION_EXPIRED -> Resource(Res.string.error_session_expired)
                }

                else -> Resource(Res.string.error_unknown)
            }
        }
    }
}

@Composable
fun UiText.asString(): String {
    return when (this) {
        is UiText.DynamicString -> value
        is UiText.Resource -> stringResource(this.res)
    }
}