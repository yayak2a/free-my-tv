package io.freemytv;

/** French copy for the custom UI. Keys are the existing English copy, as in Russian.java. */
final class French {
    private French() {}

    static String text(String english) {
        switch (english) {
            case "Live TV": return "TV en direct";
            case "All Apps": return "Toutes les applis";
            case "Settings": return "Paramètres";
            case "Home Settings": return "Réglages de l'accueil";
            case "TV Settings": return "Paramètres de la TV";
            case "HDMI Inputs": return "Entrées HDMI";
            case "Inputs": return "Entrées";
            case "Shortcuts": return "Raccourcis";
            case "Weather": return "Météo";
            case "No weather data right now.": return "Pas de données météo pour le moment.";
            case "Sunrise ": return "Lever ";
            case "Sunset ": return "Coucher ";
            case "Crypto": return "Crypto";
            case "24h change": return "variation sur 24 h";
            case "For You": return "Pour vous";
            case "← → to move · OK to drop · Back to cancel": return "← → déplacer · OK poser · Retour annuler";
            case "Moving ": return "Déplacement de ";
            case ", position ": return ", position ";
            case "Couldn't open ": return "Impossible d'ouvrir ";
            case " of ": return " sur ";
            case ", tab ": return ", onglet ";
            case " of 3": return " sur 3";
            case "Dollar": return "Dollar";
            case "Series": return "Série";
            case "prices from ": return "cours de ";
            case "Starting…": return "Démarrage…";
            case "Original": return "Original";
            case "Home Screen, ": return "Écran d'accueil, ";
            case "Screensaver, preview": return "Économiseur d'écran, aperçu";
            case "no ads, with the new screensaver": return "sans pubs, avec le nouvel économiseur d'écran";
            case "the TV's factory home screen": return "l'écran d'accueil d'origine de la TV";
            case ". In use": return ". Actif";
            case " of 2": return " sur 2";
            case "Your TV's home screen and screensaver": return "Écran d'accueil et économiseur d'écran de la TV";
            case "Home Screen": return "Écran d'accueil";
            case "Screensaver": return "Économiseur d'écran";
            case "Nature": return "Nature";
            case "Movies and shows": return "Films et séries";
            case "Both": return "Les deux";
            case "Photo: ": return "Photo : ";
            case "Preview": return "Aperçu";
            case "On the TV: tick “Always allow” and press OK.": return "Sur la TV : cochez « Toujours autoriser » puis appuyez sur OK.";
            case "RECOMMENDATIONS": return "RECOMMANDATIONS";
            case "No TMDB key: see the README (config.properties).": return "Pas de clé TMDB : voir le README (config.properties).";
            case "Error: ": return "Erreur : ";
            case "Fetching titles…": return "Chargement des titres…";
            case "This product uses the TMDB API but is not endorsed or certified by TMDB.": return "Ce produit utilise l'API TMDB sans être approuvé ni certifié par TMDB.";
            case "COMPARISON": return "COMPARAISON";
            case "Home button": return "Bouton Accueil";
            case "Free memory": return "Mémoire libre";
            case "Home screen memory": return "Mémoire de l'écran d'accueil";
            case "NOTICES": return "NOTIFICATIONS";
            case "None yet.": return "Aucune pour l'instant.";
            case "No ads, with the new screensaver": return "Sans pubs, avec le nouvel économiseur d'écran";
            case "The TV's factory home screen": return "L'écran d'accueil d'origine de la TV";
            case "Switching takes a few seconds; the screen blinks while it measures.": return "Le changement prend quelques secondes ; l'écran clignote pendant les mesures.";
            case "now": return "à l'instant";
            case "Measuring the original home screen…": return "Mesure de l'écran d'accueil d'origine…";
            case "Switching the home screen and screensaver…": return "Changement de l'écran d'accueil et de l'économiseur d'écran…";
            case "Android didn't accept the new home screen": return "Android a refusé le nouvel écran d'accueil";
            case "Turning off apps (": return "Désactivation des applis (";
            case "Measuring Free My TV…": return "Mesure de Free My TV…";
            case "Couldn't turn on Free My TV: ": return "Impossible d'activer Free My TV : ";
            case "Turning the apps back on…": return "Réactivation des applis…";
            case "Turning apps back on (": return "Réactivation des applis (";
            case "Restoring the original home screen…": return "Retour à l'écran d'accueil d'origine…";
            case "Original mode: the factory home screen, screensaver and apps are back.": return "Mode Original : l'écran d'accueil, l'économiseur d'écran et les applis d'origine sont de retour.";
            case "the original home screen": return "l'écran d'accueil d'origine";
            case "Original mode, but I couldn't turn back on: ": return "Mode Original, mais impossible de réactiver : ";
            case "Couldn't go back to the original: ": return "Impossible de revenir à l'original : ";
            case "Free My TV's home screen crashed 3 times in a row; I switched back to the original one.": return "L'écran d'accueil de Free My TV a planté 3 fois de suite ; l'écran d'origine a été rétabli.";
            case "the home screen": return "l'écran d'accueil";
            case "the screensaver": return "l'économiseur d'écran";
            case "Couldn't check Free My TV: ": return "Impossible de vérifier Free My TV : ";
            case "The TV update turned back on ": return "La mise à jour de la TV a réactivé : ";
            case "Turned off what was missing: ": return "Désactivé ce qui manquait : ";
            case "The TV was updated; Free My TV is still on.": return "La TV a été mise à jour ; Free My TV est toujours actif.";
            case "Connecting…": return "Connexion…";
            case "Accept “Allow USB debugging?” on the TV (tick “Always allow”).": return "Acceptez « Autoriser le débogage USB ? » sur la TV (cochez « Toujours autoriser »).";
            case "Language": return "Langue";
            case "Temperature": return "Température";
            case "Theme": return "Thème";
            case "Automatic": return "Automatique";
            case "Light": return "Clair";
            case "Dark": return "Sombre";
            case "Weather widget": return "Widget météo";
            // What TalkBack reads on the drawer's weather card (#47)
            case ". High ": return ". Maximum ";
            case ", low ": return ", minimum ";
            case " Next hours: ": return " Prochaines heures : ";
            case " Sunrise ": return " Lever du soleil ";
            case " Sunset ": return " Coucher du soleil ";
            case "City": return "Ville";
            case "Searching…": return "Recherche…";
            case "Type the city's name": return "Tapez le nom de la ville";
            case "Search": return "Rechercher";
            case "Cancel": return "Annuler";
            case "No city with that name.": return "Aucune ville de ce nom.";
            case "Couldn't search now: ": return "Recherche impossible pour le moment : ";
            case "Quotes widget": return "Widget des cours";
            case "On": return "Activé";
            case "Off": return "Désactivé";
            case "Shelf": return "Barre d'applis";
            case "on the shelf, place ": return "dans la barre, place ";
            case "not on the shelf": return "pas dans la barre";
            case "Remote Buttons": return "Boutons de la télécommande";
            case "Netflix button": return "Bouton Netflix";
            case "YouTube button": return "Bouton YouTube";
            case "Prime Video button": return "Bouton Prime Video";
            case "TCL Channel button": return "Bouton TCL Channel";
            case "Nothing": return "Rien";
            case "Not installed": return "Non installé";
            case ", selected": return ", sélectionné";
            case "OK or the arrows change the value.": return "OK ou les flèches changent la valeur.";
            case "OK picks, Back cancels.": return "OK choisit, Retour annule.";
            case "Disney and Globoplay: the TV itself controls those buttons.": return "Disney et Globoplay : la TV gère elle-même ces boutons.";
            case "YouTube, Prime Video and Google Play: the TV itself controls those buttons.": return "YouTube, Prime Video et Google Play : la TV gère elle-même ces boutons.";
            case "These are the TCL remote's buttons: on this TV, the buttons work as they always did.": return "Ce sont les boutons de la télécommande TCL : sur cette TV, ils fonctionnent comme avant.";
            case "Explore": return "Découvrir";
            case "Movies": return "Films";
            case "Shows": return "Séries";
            case "Clear": return "Dégagé";
            case "Mostly Sunny": return "Plutôt ensoleillé";
            case "Mostly Clear": return "Plutôt dégagé";
            case "Partly Cloudy": return "Éclaircies";
            case "Cloudy": return "Nuageux";
            case "Fog": return "Brouillard";
            case "Drizzle": return "Bruine";
            case "Heavy Rain": return "Forte pluie";
            case "Snow": return "Neige";
            case "Thunderstorms": return "Orages";
            case "Rain": return "Pluie";
            case "On · ": return "Activé · ";
            case "OK adds or removes · ← → change the order": return "OK ajoute ou retire · ← → changent l'ordre";
            case "Couldn't open TV Settings": return "Impossible d'ouvrir les paramètres de la TV";
            case "THIS MORNING": return "CE MATIN";
            case "THIS AFTERNOON": return "CET APRÈS-MIDI";
            case "TONIGHT": return "CE SOIR";
            case "Action & Adventure": return "Action et aventure";
            case "Sci-Fi & Fantasy": return "Science-fiction et fantastique";
            case "War & Politics": return "Guerre et politique";
            case "Kids": return "Enfants";
            case "News": return "Infos";
            case "Soap": return "Feuilleton";
            case "Talk": return "Talk-show";
            case "": return "";
            case "Free My TV's home screen ran into a problem.\nGoing back to the TV's original home screen…": return "L'écran d'accueil de Free My TV a rencontré un problème.\nRetour à l'écran d'accueil d'origine de la TV…";
            case "debugging is off on the TV (port 5555 closed)": return "le débogage est désactivé sur la TV (port 5555 fermé)";
            case "debugging permission wasn't accepted on the TV": return "l'autorisation de débogage n'a pas été acceptée sur la TV";
            case "the TV's ADB didn't respond": return "l'ADB de la TV n'a pas répondu";
            case "the TV asked for TLS (wireless debugging); use USB debugging": return "la TV demande TLS (débogage sans fil) ; utilisez le débogage USB";
            case "unexpected reply from ADB": return "réponse inattendue d'ADB";
            case "the TV rejected the app's key": return "la TV a refusé la clé de l'appli";
            case "ADB refused the command": return "ADB a refusé la commande";
            case "invalid ADB message": return "message ADB invalide";
            case "ADB message too large": return "message ADB trop long";
            case "the TV's first setup saved the Wi-Fi, and turning it off would erase those networks. "
                    + "In Settings → Network & Internet, forget the network and connect again (networks out of range don't "
                    + "show there: Claude Code can remove them from your computer), then try once more.":
                return "la configuration initiale de la TV a enregistré le Wi-Fi, et la désactiver effacerait ces réseaux. "
                        + "Dans Paramètres → Réseau et Internet, oubliez le réseau puis reconnectez-vous (les réseaux hors de portée "
                        + "n'y apparaissent pas : Claude Code peut les supprimer depuis votre ordinateur), puis réessayez.";
            default:
                // Notices are saved in the language in use when they were generated.
                String[] prefixes = {"Couldn't check Free My TV: ", "Couldn't turn on Free My TV: ",
                        "Couldn't go back to the original: ", "Original mode, but I couldn't turn back on: "};
                for (String p : prefixes) if (english.startsWith(p)) return text(p) + text(english.substring(p.length()));
                String shelfPrefix = "OK adds or removes (up to ";
                String shelfSuffix = "). ← → change the order.";
                if (english.startsWith(shelfPrefix) && english.endsWith(shelfSuffix)) {
                    String n = english.substring(shelfPrefix.length(), english.length() - shelfSuffix.length());
                    return "OK ajoute ou retire (jusqu'à " + n + "). ← → changent l'ordre.";
                }
                String prefix = "Free My TV is on: new home screen and screensaver, ";
                if (english.startsWith(prefix) && english.endsWith(" apps turned off.")) {
                    String n = english.substring(prefix.length(), english.length() - " apps turned off.".length());
                    return "Free My TV est actif : nouvel écran d'accueil et économiseur d'écran, " + n + " applis désactivées.";
                }
                if (english.startsWith("On · ") && english.endsWith(" titles from the TV's apps, 3 new ones every hour.")) {
                    String n = english.substring(5, english.length() - " titles from the TV's apps, 3 new ones every hour.".length());
                    return "Activé · " + n + " titres des applis de la TV, 3 nouveaux chaque heure.";
                }
                if (english.endsWith(" min ago")) return "il y a " + english.substring(0, english.length() - 8) + " min";
                if (english.endsWith(" h ago")) return "il y a " + english.substring(0, english.length() - 6) + " h";
                if (english.endsWith(" days ago")) {
                    String n = english.substring(0, english.length() - 9);
                    return "il y a " + n + ("1".equals(n) ? " jour" : " jours");
                }
                return english;
        }
    }
}
