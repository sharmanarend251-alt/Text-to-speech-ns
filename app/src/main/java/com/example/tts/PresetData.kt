package com.example.tts

import java.util.Locale

data class TtsLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val locale: Locale,
    val flagEmoji: String
)

data class PresetCategory(
    val id: String,
    val title: String,
    val hindiTitle: String,
    val iconName: String,
    val phrases: List<PresetPhrase>
)

data class PresetPhrase(
    val id: String,
    val title: String,
    val text: String,
    val languageCode: String,
    val categoryId: String,
    val description: String? = null
)

object PresetData {

    val SUPPORTED_LANGUAGES = listOf(
        TtsLanguage("hi-IN", "Hindi", "हिन्दी (भारत)", Locale("hi", "IN"), "🇮🇳"),
        TtsLanguage("en-IN", "English (India)", "English (India)", Locale("en", "IN"), "🇮🇳"),
        TtsLanguage("en-US", "English (US)", "English (United States)", Locale("en", "US"), "🇺🇸"),
        TtsLanguage("en-GB", "English (UK)", "English (United Kingdom)", Locale("en", "GB"), "🇬🇧"),
        TtsLanguage("es-ES", "Spanish", "Español", Locale("es", "ES"), "🇪🇸"),
        TtsLanguage("fr-FR", "French", "Français", Locale("fr", "FR"), "🇫🇷"),
        TtsLanguage("de-DE", "German", "Deutsch", Locale("de", "DE"), "🇩🇪"),
        TtsLanguage("ja-JP", "Japanese", "日本語", Locale("ja", "JP"), "🇯🇵"),
        TtsLanguage("ru-RU", "Russian", "Русский", Locale("ru", "RU"), "🇷🇺"),
        TtsLanguage("bn-IN", "Bengali", "বাংলা", Locale("bn", "IN"), "🇮🇳"),
        TtsLanguage("mr-IN", "Marathi", "मराठी", Locale("mr", "IN"), "🇮🇳"),
        TtsLanguage("ta-IN", "Tamil", "தமிழ்", Locale("ta", "IN"), "🇮🇳"),
        TtsLanguage("te-IN", "Telugu", "తెలుగు", Locale("te", "IN"), "🇮🇳"),
        TtsLanguage("gu-IN", "Gujarati", "ગુજરાતી", Locale("gu", "IN"), "🇮🇳")
    )

    val DEFAULT_LANGUAGE = SUPPORTED_LANGUAGES[0] // Hindi as default for user request

    val CATEGORIES = listOf(
        PresetCategory(
            id = "hindi_daily",
            title = "Daily Hindi",
            hindiTitle = "दैनिक हिंदी वाक्य",
            iconName = "Chat",
            phrases = listOf(
                PresetPhrase(
                    id = "h1",
                    title = "Greeting / अभिवादन",
                    text = "नमस्ते! आपका दिन बहुत ही शुभ और मंगलमय हो।",
                    languageCode = "hi-IN",
                    categoryId = "hindi_daily",
                    description = "Polite morning/daily greeting"
                ),
                PresetPhrase(
                    id = "h2",
                    title = "Thank You / आभार",
                    text = "आपकी बहुमूल्य सहायता और मार्गदर्शन के लिए बहुत-बहुत धन्यवाद।",
                    languageCode = "hi-IN",
                    categoryId = "hindi_daily",
                    description = "Expressing gratitude"
                ),
                PresetPhrase(
                    id = "h3",
                    title = "Inspirational / प्रेरणादायक",
                    text = "कोशिश करने वालों की कभी हार नहीं होती, लहरों से डर कर नौका पार नहीं होती।",
                    languageCode = "hi-IN",
                    categoryId = "hindi_daily",
                    description = "Classic inspirational poem line"
                ),
                PresetPhrase(
                    id = "h4",
                    title = "Question / पूछताछ",
                    text = "कृपया क्या आप मुझे बता सकते हैं कि निकटतम बस स्टॉप कहाँ है?",
                    languageCode = "hi-IN",
                    categoryId = "hindi_daily",
                    description = "Asking directions"
                ),
                PresetPhrase(
                    id = "h5",
                    title = "Introduction / परिचय",
                    text = "मेरा नाम राहुल है। मैं तकनीक और नवाचार में गहरी रुचि रखता हूँ।",
                    languageCode = "hi-IN",
                    categoryId = "hindi_daily",
                    description = "Self-introduction"
                )
            )
        ),
        PresetCategory(
            id = "english_daily",
            title = "English Practice",
            hindiTitle = "अंग्रेजी अभ्यास",
            iconName = "RecordVoiceOver",
            phrases = listOf(
                PresetPhrase(
                    id = "e1",
                    title = "Welcome Message",
                    text = "Welcome to the Free Text to Speech app! Transform any written text into natural human voice instantly.",
                    languageCode = "en-US",
                    categoryId = "english_daily",
                    description = "Introduction and overview"
                ),
                PresetPhrase(
                    id = "e2",
                    title = "Business Etiquette",
                    text = "Thank you for joining today's meeting. Let us review the key objectives and action items.",
                    languageCode = "en-US",
                    categoryId = "english_daily",
                    description = "Professional meeting opening"
                ),
                PresetPhrase(
                    id = "e3",
                    title = "Customer Support",
                    text = "Your request has been successfully processed. Please let us know if you need any further assistance.",
                    languageCode = "en-US",
                    categoryId = "english_daily",
                    description = "Polite customer support closing"
                ),
                PresetPhrase(
                    id = "e4",
                    title = "Wisdom Quote",
                    text = "Success is not final, failure is not fatal: it is the courage to continue that counts.",
                    languageCode = "en-US",
                    categoryId = "english_daily",
                    description = "Inspirational quote"
                )
            )
        ),
        PresetCategory(
            id = "tongue_twisters",
            title = "Tongue Twisters",
            hindiTitle = "बोलने का अभ्यास (टंग ट्विस्टर)",
            iconName = "GraphicEq",
            phrases = listOf(
                PresetPhrase(
                    id = "tt1",
                    title = "खड़क सिंह (Hindi)",
                    text = "खड़क सिंह के खड़कने से खड़कती हैं खिड़कियां, खिड़कियों के खड़कने से खड़कता है खड़क सिंह।",
                    languageCode = "hi-IN",
                    categoryId = "tongue_twisters",
                    description = "Popular Hindi tongue twister"
                ),
                PresetPhrase(
                    id = "tt2",
                    title = "पके पेड़ पर पका पपीता",
                    text = "पके पेड़ पर पका पपीता, पका पेड़ या पका पपीता, पके पेड़ को पकड़े पिंकू, पिंकू पकड़े पका पपीता।",
                    languageCode = "hi-IN",
                    categoryId = "tongue_twisters",
                    description = "Famous Hindi rhyming twister"
                ),
                PresetPhrase(
                    id = "tt3",
                    title = "Peter Piper (English)",
                    text = "Peter Piper picked a peck of pickled peppers. A peck of pickled peppers Peter Piper picked.",
                    languageCode = "en-US",
                    categoryId = "tongue_twisters",
                    description = "Classic English twister"
                ),
                PresetPhrase(
                    id = "tt4",
                    title = "She Sells Seashells",
                    text = "She sells seashells by the seashore. The shells she sells are surely seashells.",
                    languageCode = "en-GB",
                    categoryId = "tongue_twisters",
                    description = "Speed test pronunciation"
                )
            )
        ),
        PresetCategory(
            id = "announcements",
            title = "Announcements",
            hindiTitle = "उद्घोषणा व सूचना",
            iconName = "Campaign",
            phrases = listOf(
                PresetPhrase(
                    id = "a1",
                    title = "Station Announcement",
                    text = "यात्रीगण कृपया ध्यान दें, गाड़ी संख्या एक दो तीन चार पांच, प्लेटफार्म नंबर दो पर आ रही है।",
                    languageCode = "hi-IN",
                    categoryId = "announcements",
                    description = "Railway station announcement"
                ),
                PresetPhrase(
                    id = "a2",
                    title = "Flight Boarding",
                    text = "Attention all passengers, flight 402 is now boarding at Gate Number 7. Please have your boarding passes ready.",
                    languageCode = "en-US",
                    categoryId = "announcements",
                    description = "Airport announcement"
                ),
                PresetPhrase(
                    id = "a3",
                    title = "Attention Call",
                    text = "कृपया सभी उपस्थित जन शांति बनाए रखें, कार्यक्रम कुछ ही पलों में प्रारंभ होने जा रहा है।",
                    languageCode = "hi-IN",
                    categoryId = "announcements",
                    description = "Event announcement"
                )
            )
        )
    )
}
