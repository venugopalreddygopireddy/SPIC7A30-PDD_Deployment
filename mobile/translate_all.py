import os
import re
import xml.etree.ElementTree as ET

strings_dict = {
  "about_help": {
    "en": "About & Help", "hi": "विवरण और सहायता", "te": "గురించి & సహాయం", "ta": "பற்றி & உதவி", "fr": "À propos & Aide"
  },
  "anonymous_research": {
    "en": "Anonymous Research", "hi": "अनाम शोध", "te": "అనామక పరిశోధన", "ta": "அநாமதேய ஆராய்ச்சி", "fr": "Recherche Anonyme"
  },
  "appearance": {
    "en": "Appearance", "hi": "दिखावट", "te": "స్వరూపం", "ta": "தோற்றம்", "fr": "Apparence"
  },
  "buy_now_2000": {
    "en": "Buy Now (2000 🪙)", "hi": "अभी खरीदें (2000 🪙)", "te": "ఇప్పుడే కొనండి (2000 🪙)", "ta": "இப்போது வாங்கவும் (2000 🪙)", "fr": "Acheter (2000 🪙)"
  },
  "csv_format": {
    "en": "CSV", "hi": "CSV", "te": "CSV", "ta": "CSV", "fr": "CSV"
  },
  "choose_format": {
    "en": "Choose your preferred format for the wellness report:", "hi": "वेलनेस रिपोर्ट के लिए अपना पसंदीदा प्रारूप चुनें:", "te": "వెల్నెస్ నివేదిక కోసం మీ ప్రాధాన్య ఆకృతిని ఎంచుకోండి:", "ta": "வெல்னஸ் அறிக்கைக்கான உங்கள் விருப்பமான வடிவத்தைத் தேர்ந்தெடுக்கவும்:", "fr": "Choisissez votre format préféré pour le rapport de bien-être:"
  },
  "close_btn": {
    "en": "Close", "hi": "बंद करें", "te": "మూసివేయి", "ta": "மூடு", "fr": "Fermer"
  },
  "contact_support": {
    "en": "Contact Support", "hi": "संपर्क सहायता", "te": "మద్దతును సంప్రదించండి", "ta": "ஆதரவை தொடர்பு கொள்ளவும்", "fr": "Contacter le support"
  },
  "cost_2000_coins": {
    "en": "Cost: 2000 Wellness Coins 🪙", "hi": "लागत: 2000 वेलनेस सिक्के 🪙", "te": "ఖర్చు: 2000 వెల్నెస్ నాణేలు 🪙", "ta": "செலவு: 2000 ஆரோக்கிய நாணயங்கள் 🪙", "fr": "Coût: 2000 pièces de bien-être 🪙"
  },
  "current_streak_title": {
    "en": "Current Streak", "hi": "वर्तमान स्ट्रीक", "te": "ప్రస్తుత పరంపర", "ta": "தற்போதைய ஸ்ட்ரீக்", "fr": "Série actuelle"
  },
  "dark_mode_title": {
    "en": "Dark Mode", "hi": "डार्क मोड", "te": "డార్క్ మోడ్", "ta": "டார்க் மோடு", "fr": "Mode Sombre"
  },
  "download_my_data": {
    "en": "Download My Data", "hi": "मेरा डेटा डाउनलोड करें", "te": "నా డేటాను డౌన్‌లోడ్ చేయండి", "ta": "என் தரவிறக்கம்", "fr": "Télécharger mes données"
  },
  "edit_profile_title": {
    "en": "Edit Profile", "hi": "प्रोफ़ाइल संपादित करें", "te": "ప్రొఫైల్‌ని సవరించండి", "ta": "சுயவிவரத்தை திருத்து", "fr": "Modifier le profil"
  },
  "great_btn": {
    "en": "Great!", "hi": "बढ़िया!", "te": "గొప్పది!", "ta": "மிக நன்று!", "fr": "Génial!"
  },
  "i_understand_btn": {
    "en": "I Understand", "hi": "मैं समझता हूँ", "te": "నాకు అర్థమైంది", "ta": "நான் புரிந்து கொண்டேன்", "fr": "Je comprends"
  },
  "language_title": {
    "en": "Language", "hi": "भाषा", "te": "భాష", "ta": "மொழி", "fr": "Langue"
  },
  "maybe_later": {
    "en": "Maybe Later", "hi": "शायद बाद में", "te": "బహుశా తర్వాత", "ta": "ஒருவேளை பிறகு", "fr": "Peut-être plus tard"
  },
  "no_factors_detected": {
    "en": "No factors detected.", "hi": "कोई कारक नहीं पाया गया।", "te": "ఎలాంటి కారకాలు కనుగొనబడలేదు.", "ta": "காரணிகள் எதுவும் கண்டறியப்படவில்லை.", "fr": "Aucun facteur détecté."
  },
  "pdf_format": {
    "en": "PDF", "hi": "PDF", "te": "PDF", "ta": "PDF", "fr": "PDF"
  },
  "privacy_and_data": {
    "en": "Privacy & Data", "hi": "गोपनीयता और डेटा", "te": "గోప్యత & డేటా", "ta": "தனியுரிமை மற்றும் தரவு", "fr": "Confidentialité et données"
  },
  "push_notifications": {
    "en": "Push Notifications", "hi": "पुश सूचनाएं", "te": "పుష్ నోటిఫికేషన్‌లు", "ta": "புஷ் அறிவிப்புகள்", "fr": "Notifications push"
  },
  "rate_us": {
    "en": "Rate Us", "hi": "हमें रेट करें", "te": "మమ్మల్ని రేట్ చేయండి", "ta": "எங்களை மதிப்பிடுங்கள்", "fr": "Évaluez-nous"
  },
  "terms_and_privacy": {
    "en": "Terms & Privacy", "hi": "शर्तें और गोपनीयता", "te": "నిబంధనలు & గోప్యత", "ta": "விதிமுறைகள் & தனியுரிமை", "fr": "Conditions et confidentialité"
  },
  "terms_privacy_dialog": {
    "en": "Terms & Privacy 🔒", "hi": "शर्तें और गोपनीयता 🔒", "te": "నిబంధనలు & గోప్యత 🔒", "ta": "விதிமுறைகள் & தனியுரிமை 🔒", "fr": "Conditions et confidentialité 🔒"
  },
  "thank_you_premium": {
    "en": "Thank you for being a premium member! You have access to all advanced AI features and personalized insights.", "hi": "प्रीमियम सदस्य होने के लिए धन्यवाद! आपके पास सभी उन्नत एआई सुविधाओं तक पहुंच है।", "te": "ప్రీమియం మెంబర్ అయినందుకు ధన్యవాదాలు! మీరు అన్ని అధునాతన AI లక్షణాలకు ప్రాప్యతను కలిగి ఉన్నారు.", "ta": "பிரீமியம் உறுப்பினராக இருப்பதற்கு நன்றி!", "fr": "Merci d'être un membre premium!"
  },
  "theme_title": {
    "en": "Theme", "hi": "थीम", "te": "థీమ్", "ta": "தீம்", "fr": "Thème"
  },
  "unlock_ai_potential": {
    "en": "Unlock full AI potential, advanced wellness reports, and ad-free experience!", "hi": "पूर्ण एआई क्षमता, उन्नत वेलनेस रिपोर्ट अनलॉक करें!", "te": "పూర్తి AI సామర్థ్యాన్ని అన్‌లాక్ చేయండి!", "ta": "முழு AI திறனையும் திறக்கவும்!", "fr": "Débloquez tout le potentiel de l'IA!"
  },
  "usage_analytics": {
    "en": "Usage Analytics", "hi": "उपयोग विश्लेषण", "te": "వినియోగ విశ్లేషణలు", "ta": "பயன்பாட்டு பகுப்பாய்வு", "fr": "Analyse d'utilisation"
  },
  "version_1_1_0": {
    "en": "Version 1.1.0", "hi": "संस्करण 1.1.0", "te": "వెర్షన్ 1.1.0", "ta": "பதிப்பு 1.1.0", "fr": "Version 1.1.0"
  },
  "whats_new": {
    "en": "What's New", "hi": "नया क्या है", "te": "కొత్తది ఏమిటి", "ta": "புதியது என்ன", "fr": "Quoi de neuf"
  },
  "whats_new_dialog": {
    "en": "What's New in v1.1.0 🚀", "hi": "v1.1.0 में नया क्या है 🚀", "te": "v1.1.0లో కొత్తది ఏమిటి 🚀", "ta": "v1.1.0 இல் புதியது என்ன 🚀", "fr": "Quoi de neuf dans la v1.1.0 🚀"
  },
  "privacy_policy_text": {
    "en": "Your data is stored locally and securely. We only use your feedback to improve our AI models. We do not sell your personal data to third parties.", "hi": "आपका डेटा स्थानीय और सुरक्षित रूप से संग्रहीत है।", "te": "మీ డేటా స్థానికంగా మరియు సురక్షితంగా నిల్వ చేయబడుతుంది.", "ta": "உங்கள் தரவு உள்ளூரில் மற்றும் பாதுகாப்பாக சேமிக்கப்பட்டுள்ளது.", "fr": "Vos données sont stockées localement et en toute sécurité."
  },
  "changelog_1": {
    "en": "• Added Wellness Coins system 🪙", "hi": "• वेलनेस कॉइन्स सिस्टम जोड़ा गया 🪙", "te": "• వెల్నెస్ కాయిన్స్ సిస్టమ్ జోడించబడింది 🪙", "ta": "• ஆரோக்கிய நாணயங்கள் அமைப்பு சேர்க்கப்பட்டது 🪙", "fr": "• Système de pièces de bien-être ajouté 🪙"
  },
  "changelog_2": {
    "en": "• Download your data as PDF/CSV 📄", "hi": "• अपना डेटा PDF/CSV के रूप में डाउनलोड करें 📄", "te": "• మీ డేటాను PDF/CSV గా డౌన్‌లోడ్ చేయండి 📄", "ta": "• உங்கள் தரவை PDF/CSV ஆக பதிவிறக்கவும் 📄", "fr": "• Téléchargez vos données au format PDF/CSV 📄"
  },
  "changelog_3": {
    "en": "• New 'Zomato-style' witty alerts 🍕", "hi": "• नए चतुर अलर्ट 🍕", "te": "• కొత్త చమత్కార హెచ్చరికలు 🍕", "ta": "• புதிய சுவையான விழிப்பூட்டல்கள் 🍕", "fr": "• Nouvelles alertes amusantes 🍕"
  },
  "changelog_4": {
    "en": "• Performance improvements & bug fixes 🛠️", "hi": "• प्रदर्शन में सुधार और बग फिक्स 🛠️", "te": "• పనితీరు మెరుగుదలలు & బగ్ పరిష్కారాలు 🛠️", "ta": "• செயல்திறன் மேம்பாடுகள் & பிழை திருத்தங்கள் 🛠️", "fr": "• Améliorations des performances et corrections de bugs 🛠️"
  },
  "changelog_5": {
    "en": "• Smart Stress-based notifications 🔔", "hi": "• स्मार्ट तनाव-आधारित सूचनाएं 🔔", "te": "• స్మార్ట్ ఒత్తిడి-ఆధారిత నోటిఫికేషన్‌లు 🔔", "ta": "• ஸ்மார்ட் மன அழுத்த அடிப்படையிலான அறிவிப்புகள் 🔔", "fr": "• Notifications intelligentes basées sur le stress 🔔"
  },
  "help_and_support": {
    "en": "Help & Support", "hi": "सहायता और समर्थन", "te": "సహాయం & మద్దతు", "ta": "உதவி & ஆதரவு", "fr": "Aide et support"
  },
  "daily_reminders": {
    "en": "Daily Reminders", "hi": "दैनिक अनुस्मारक", "te": "రోజువారీ రిమైండర్‌లు", "ta": "தினசரி நினைவூட்டல்கள்", "fr": "Rappels quotidiens"
  },
  "stress_level_legend": {
    "en": "Stress Level Legend", "hi": "तनाव स्तर लीजेंड", "te": "ఒత్తిడి స్థాయి లెజెండ్", "ta": "மன அழுத்த நிலை லெஜண்ட்", "fr": "Légende du niveau de stress"
  },
  "send_reset_link": {
    "en": "Send Reset Link", "hi": "रीसेट लिंक भेजें", "te": "రీసెట్ లింక్‌ను పంపండి", "ta": "மீட்டமைப்பு இணைப்பை அனுப்பு", "fr": "Envoyer le lien de réinitialisation"
  }
}

paths = {
  "en": "app/src/main/res/values/strings.xml",
  "hi": "app/src/main/res/values-hi/strings.xml",
  "te": "app/src/main/res/values-te/strings.xml",
  "ta": "app/src/main/res/values-ta/strings.xml",
  "fr": "app/src/main/res/values-fr/strings.xml"
}

def update_xml(lang):
    file_path = paths[lang]
    if not os.path.exists(file_path): return
    tree = ET.parse(file_path)
    root = tree.getroot()
    
    existing_keys = {child.attrib['name'] for child in root if 'name' in child.attrib}
    
    for key, trans in strings_dict.items():
        if key not in existing_keys:
            el = ET.Element("string", name=key)
            el.text = trans[lang]
            root.append(el)
            
    xml_str = ET.tostring(root, encoding='utf-8', xml_declaration=True).decode('utf-8')
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(xml_str.replace('><', '>\n    <').replace('</resources>', '\n</resources>'))

for l in paths.keys():
    update_xml(l)
    
print("Updated all strings.xml")

with open('app/src/main/java/com/cortisense/app/MainActivity.kt', 'r', encoding='utf-8') as f:
    kt = f.read()

# Replace direct strings with stringResource(R.string.key)
for key, trans in strings_dict.items():
    eng_str = trans["en"]
    # Be careful with quotes inside the string, but here we don't have nested double quotes
    pattern1 = f'"{re.escape(eng_str)}"'
    kt = kt.replace(pattern1, f'stringResource(R.string.{key})')

with open('app/src/main/java/com/cortisense/app/MainActivity.kt', 'w', encoding='utf-8') as f:
    f.write(kt)

print("Updated MainActivity.kt")
