package com.feedback.android.app.common

object Constants {

    const val BASE_URL = "https://оставьте-мне-отзыв.рф/api/"
    const val OPERATE_PERSONAL_DATA_URL = "https://оставьте-мне-отзыв.рф/operate/personal/data"
    const val SUBSCRIPTION_RULES_URL = "https://оставьте-мне-отзыв.рф/subscription/rules"

    const val FIND_USER_BY_PHONE_URL = "find/user/by/phone"
    const val AUTH_URL = "auth"
    const val GET_USER_DATA_URL = "get/user/data/{id}"
    const val SAVE_USER_DATA_URL = "save/user/data"
    const val SET_NEW_PIN_CODE_URL = "change/pincode"
    const val TARIFFS_URL = "tariffs/{currentUserTariffId}"
    const val SEND_PIN_CODE_URL = "send/pincode"
    const val CHECK_PIN_CODE_URL = "check/pincode"
    const val SET_USER_TARIFF = "set/tariff/{id}"
    const val CANCEL_USER_TARIFF = "cancel/tariff/{id}"
    const val GET_ALL_USERS = "get/all/users"
    const val GET_ALL_FEEDBACKS = "get/all/feedbacks"
    const val PAY_TARIFF = "pay/tariff"
    const val CHECK_PAYMENT = "check/payment/{user_id}/{tariff_id}"

    const val MODERATOR_SEARCH_USERS = "moderator/users/search"
    const val MODERATOR_DELETE_USER = "moderator/users/delete"

    const val IS_AUTH_USER_KEY = "is_auth"
    const val USER_ID = "user_id"
    const val LAST_VISITED_TIME = "last_visited_time"

}