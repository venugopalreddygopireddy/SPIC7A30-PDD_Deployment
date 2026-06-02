import json
import xml.etree.ElementTree as ET

# Telugu Translations
te_dict = {
    "app_name": "కార్టిసెన్స్ (CortiSense)",
    "welcome": "కార్టిసెన్స్ కు స్వాగతం",
    "get_started": "ప్రారంభించండి",
    "sign_in": "సైన్ ఇన్",
    "good_morning": "శుభోదయం,",
    "stress_score": "ఒత్తిడి స్కోరు",
    "home": "హోమ్",
    "analytics": "విశ్లేషణలు",
    "checkin": "చెక్-ఇన్",
    "ai_chat": "ఏఐ చాట్",
    "profile": "ప్రొఫైల్",
    "settings": "సెట్టింగ్స్",
    "language": "భాష",
    "dark_mode": "డార్క్ మోడ్",
    "logout": "లాగ్ అవుట్",
    "analyzing": "విశ్లేషిస్తోంది",
    "results_title": "ఫలితాలు",
    "main_reasons": "ప్రధాన కారణాలు:",
    "view_insights": "వివరాలు చూడండి",
    "recheck": "మళ్ళీ చెక్ చేయండి",
    "tagline": "ఒత్తిడిని అంచనా వేయండి. నివారించండి.",
    "age": "వయసు",
    "enter_age": "వయసు నమోదు చేయండి",
    "gender": "లింగం",
    "select_gender": "లింగం ఎంచుకోండి",
    "reduce_stress": "ఒత్తిడిని తగ్గించండి",
    "better_sleep": "మంచి నిద్ర",
    "improve_focus": "శ్రద్ధను మెరుగుపరచండి",
    "continue_btn": "కొనసాగించండి",
    "how_it_works": "ఇది ఎలా పనిచేస్తుంది",
    "track": "1. ట్రాక్ చేయండి",
    "analyze": "2. విశ్లేషించండి",
    "improve": "3. మెరుగుపరచుకోండి",
    "welcome_back": "తిరిగి స్వాగతం",
    "email_address": "ఈమెయిల్",
    "password": "పాస్వర్డ్",
    "forgot_password": "పాస్వర్డ్ మర్చిపోయారా?",
    "signup": "సైన్ అప్",
    "create_account": "ఖాతా సృష్టించండి",
    "full_name": "పూర్తి పేరు",
    "confirm_password": "పాస్వర్డ్ నిర్ధారించండి",
    "reset_password": "పాస్వర్డ్ రీసెట్",
    "send_reset_link": "లింక్ పంపండి",
    "daily_checkin": "రోజువారీ చెక్-ఇన్",
    "analyze_stress": "ఒత్తిడిని విశ్లేషించండి",
    "avg_this_week": "ఈ వారం సగటు",
    "streak": "వరుస రోజులు",
    "weekly_trend": "వారపు ట్రెండ్",
    "last_7_days": "గత 7 రోజులు",
    "low": "తక్కువ",
    "moderate": "మధ్యస్థం",
    "high": "ఎక్కువ",
    "stress_trends": "ఒత్తిడి ట్రెండ్స్",
    "overall_trend": "మొత్తం ట్రెండ్",
    "improving": "మెరుగుపడుతోంది",
    "current_avg": "ప్రస్తుత సగటు",
    "monthly_progress": "నెలవారీ ప్రగతి",
    "key_insight": "ముఖ్యమైన గమనిక",
    "weekly_report": "వారపు రిపోర్ట్",
    "avg_heart_rate": "సగటు హృదయ స్పందన",
    "avg_sleep": "సగటు నిద్ర",
    "energy_level": "శక్తి స్థాయి",
    "best_day": "ఉత్తమ రోజు",
    "excellent": "అద్భుతం",
    "history_log": "చరిత్ర",
    "stress_calendar": "క్యాలెండర్",
    "edit_profile": "ప్రొఫైల్ ఎడిట్",
    "save_profile": "సేవ్ చేయండి",
    "name_label": "పేరు",
    "chat_error": "కనెక్ట్ కావడంలో సమస్య ఉంది. దయచేసి మళ్ళీ ప్రయత్నించండి.",
    "chat_stress_reply": "మీ డేటా ప్రకారం, మీ ఒత్తిడి %1$s.",
    "chat_stress_high": "మీరు కొన్ని బ్రీతింగ్ వ్యాయామాలు చేయండి.",
    "chat_stress_low": "మీరు చాలా బాగున్నారు!",
    "chat_sleep_reply": "మీ సగటు నిద్ర %1$s. 7-9 గంటలు నిద్రపోవడానికి ప్రయత్నించండి.",
    "chat_hello": "నమస్కారం! నేను మీ కోర్టిసెన్స్ సహాయకుడిని. నేను మీకు ఎలా సహాయపడగలను?",
    "chat_default": "అర్థమైంది. మీరు మీ ట్రెండ్స్ చూడాలనుకుంటున్నారా లేదా వ్యాయామం చేయాలనుకుంటున్నారా?",
    "extracted_how_is_your_mood_rig": "ప్రస్తుతం మీ మూడ్ ఎలా ఉంది?",
    "extracted_are_you_feeling_anxi": "మీరు ఆందోళనగా ఉన్నారా?",
    "extracted_how_many_hours_did_y": "మీరు ఎన్ని గంటలు నిద్రపోయారు?",
    "extracted_how_much_caffeine_to": "ఈరోజు కెఫిన్ ఎంత తీసుకున్నారు?",
    "extracted_screen_usage_today_": "ఈరోజు స్క్రీన్ వినియోగం?",
    "extracted_how_busy_was_your_da": "ఈరోజు ఎంత బిజీగా ఉన్నారు?",
    "extracted_how_does_your_body_f": "మీ శరీరం ఎలా అనిపిస్తోంది?",
    "extracted_good": "బాగుంది",
    "extracted_okay": "పర్వాలేదు",
    "extracted_bad": "బాగోలేదు",
    "extracted_very_fast": "చాలా వేగంగా",
    "extracted_calm": "ప్రశాంతంగా",
    "extracted_fast": "వేగంగా",
    "extracted_change_photo": "ఫోటో మార్చండి",
    "extracted_select_language": "భాషను ఎంచుకోండి",
    "extracted_cancel": "రద్దు చేయి",
    "extracted_overview": "సంగ్రహం",
    "extracted_trends": "ట్రెండ్స్",
    "extracted_report": "రిపోర్ట్",
    "extracted_monthly": "నెలవారీ",
    "extracted_view_all": "అన్నీ చూడండి",
    "extracted_heart_rate": "హృదయ స్పందన",
    "extracted_sleep": "నిద్ర",
    "extracted_recent_check_ins": "ఇటీవలి చెక్-ఇన్‌లు",
    "extracted_bpm": "bpm",
    "extracted_hrs": "గంటలు",
    "extracted_ask_me_anything": "నన్ను ఏదైనా అడగండి",
    "extracted_ask_about_your_stres": "మీ ఒత్తిడి ప్యాటర్న్స్ గురించి అడగండి...",
    "extracted_cortisense": "కార్టిసెన్స్ (CortiSense)"
}

with open('strings_dump.json', 'r', encoding='utf-8') as f:
    d = json.load(f)

for lang, transl_dict in [('te', te_dict)]:
    tree = ET.parse(f'app/src/main/res/values-{lang}/strings.xml')
    root = tree.getroot()
    
    for child in root:
        if 'name' in child.attrib:
            key = child.attrib['name']
            if key in transl_dict:
                child.text = transl_dict[key]
            else:
                # If not specifically translated, at least remove the [TE] prefix
                # and maybe provide a generic fallback without bracket
                val = d.get(key, key)
                child.text = val
                
    xml_str = ET.tostring(root, encoding='utf-8', xml_declaration=True).decode('utf-8')
    with open(f'app/src/main/res/values-{lang}/strings.xml', 'w', encoding='utf-8') as f:
        f.write(xml_str.replace('><', '>\n    <').replace('</resources>', '\n</resources>'))

print("Telugu translations applied.")
