package io.freemytv;

import java.util.Calendar;
import java.util.Locale;

/**
 * The drawer's "Today" card: a quote or proverb for the day (built in, in French: no network, nothing to break) and a
 * child's age ("Inès is 5 months and 12 days old"), from the name and birth date set in Home Settings.
 */
final class Daily {
    private Daily() {}

    /** {text, author}; an empty author is a proverb. Public-domain authors and well-known French proverbs. */
    private static final String[][] QUOTES = {
        {"Rien ne sert de courir ; il faut partir à point.", "La Fontaine"},
        {"Le cœur a ses raisons que la raison ne connaît point.", "Pascal"},
        {"Petit à petit, l'oiseau fait son nid.", ""},
        {"Il faut cultiver notre jardin.", "Voltaire"},
        {"Après la pluie, le beau temps.", ""},
        {"Patience et longueur de temps font plus que force ni que rage.", "La Fontaine"},
        {"Qui va lentement va sûrement.", ""},
        {"Ce qui se conçoit bien s'énonce clairement, et les mots pour le dire arrivent aisément.", "Boileau"},
        {"Les petits ruisseaux font les grandes rivières.", ""},
        {"À vaincre sans péril, on triomphe sans gloire.", "Corneille"},
        {"Tout vient à point à qui sait attendre.", ""},
        {"L'homme n'est qu'un roseau, le plus faible de la nature ; mais c'est un roseau pensant.", "Pascal"},
        {"La nuit porte conseil.", ""},
        {"On a souvent besoin d'un plus petit que soi.", "La Fontaine"},
        {"Rome ne s'est pas faite en un jour.", ""},
        {"Le travail éloigne de nous trois grands maux : l'ennui, le vice et le besoin.", "Voltaire"},
        {"Vouloir, c'est pouvoir.", ""},
        {"Hâtez-vous lentement, et sans perdre courage, vingt fois sur le métier remettez votre ouvrage.", "Boileau"},
        {"L'union fait la force.", ""},
        {"On n'est jamais si heureux ni si malheureux qu'on s'imagine.", "La Rochefoucauld"},
        {"Un tiens vaut mieux que deux tu l'auras.", "La Fontaine"},
        {"C'est en forgeant qu'on devient forgeron.", ""},
        {"Il faut manger pour vivre et non pas vivre pour manger.", "Molière"},
        {"Qui ne risque rien n'a rien.", ""},
        {"Le hasard ne favorise que les esprits préparés.", "Pasteur"},
        {"À cœur vaillant rien d'impossible.", ""},
        {"Ce n'est pas parce que les choses sont difficiles que nous n'osons pas, c'est parce que nous n'osons pas qu'elles sont difficiles.", "Sénèque"},
        {"Qui veut voyager loin ménage sa monture.", ""},
        {"Aide-toi, le Ciel t'aidera.", "La Fontaine"},
        {"Il n'est jamais trop tard pour bien faire.", ""},
        {"Aux âmes bien nées, la valeur n'attend point le nombre des années.", "Corneille"},
        {"Mieux vaut prévenir que guérir.", ""},
        {"Rien n'est plus contagieux que l'exemple.", "La Rochefoucauld"},
        {"Une hirondelle ne fait pas le printemps.", ""},
        {"Le mieux est l'ennemi du bien.", "Voltaire"},
        {"Les bons comptes font les bons amis.", ""},
        {"Tout flatteur vit aux dépens de celui qui l'écoute.", "La Fontaine"},
        {"Pas de nouvelles, bonnes nouvelles.", ""},
        {"L'homme est né libre, et partout il est dans les fers.", "Rousseau"},
        {"L'appétit vient en mangeant.", "Rabelais"},
        {"Il ne faut pas vendre la peau de l'ours avant de l'avoir tué.", ""},
        {"Il n'y a ni mauvaises herbes ni mauvais hommes. Il n'y a que de mauvais cultivateurs.", "Victor Hugo"},
        {"Qui trop embrasse mal étreint.", ""},
        {"La raison du plus fort est toujours la meilleure.", "La Fontaine"},
        {"Il faut tourner sept fois sa langue dans sa bouche avant de parler.", ""},
        {"Rien ne pèse tant qu'un secret.", "La Fontaine"},
        {"Ce sont les tonneaux vides qui font le plus de bruit.", ""},
        {"Le temps ne fait rien à l'affaire.", "Molière"},
        {"À chaque jour suffit sa peine.", ""},
        {"Que sais-je ?", "Montaigne"},
        {"Rira bien qui rira le dernier.", ""},
        {"La musique exprime ce qui ne peut être dit et sur quoi il est impossible de rester silencieux.", "Victor Hugo"},
        {"La fortune sourit aux audacieux.", ""},
        {"Je pense, donc je suis.", "Descartes"},
        {"Plus on est de fous, plus on rit.", ""},
        {"Chaque homme porte la forme entière de l'humaine condition.", "Montaigne"},
        {"Quand le chat n'est pas là, les souris dansent.", ""},
        {"L'hypocrisie est un hommage que le vice rend à la vertu.", "La Rochefoucauld"},
        {"Chat échaudé craint l'eau froide.", ""},
        {"Rien ne se perd, rien ne se crée, tout se transforme.", "Lavoisier"},
        {"Les chiens aboient, la caravane passe.", ""},
        {"Nous avons tous assez de force pour supporter les maux d'autrui.", "La Rochefoucauld"},
        {"Bien mal acquis ne profite jamais.", ""},
        {"Ventre affamé n'a point d'oreilles.", "La Fontaine"},
        {"On ne fait pas d'omelette sans casser des œufs.", ""},
        {"Le moi est haïssable.", "Pascal"},
        {"À l'impossible nul n'est tenu.", ""},
        {"Parce que c'était lui, parce que c'était moi.", "Montaigne"},
        {"Au royaume des aveugles, les borgnes sont rois.", ""},
        {"La critique est aisée, et l'art est difficile.", "Destouches"},
        {"De la discussion jaillit la lumière.", ""},
        {"Une injustice faite à un seul est une menace faite à tous.", "Montesquieu"},
        {"En avril, ne te découvre pas d'un fil ; en mai, fais ce qu'il te plaît.", ""},
        {"Il faut laisser le temps au temps.", ""},
        {"L'erreur est humaine.", ""},
        {"La parole est d'argent, le silence est d'or.", ""},
        {"Les grands esprits se rencontrent.", ""},
        {"Mieux vaut être seul que mal accompagné.", ""},
        {"Noblesse oblige.", ""},
        {"On récolte ce que l'on sème.", ""},
        {"Qui cherche trouve.", ""},
        {"Qui peut le plus peut le moins.", ""},
        {"Tout est bien qui finit bien.", ""},
        {"Les conseilleurs ne sont pas les payeurs.", ""},
        {"Qui paie ses dettes s'enrichit.", ""},
        {"Le soleil brille pour tout le monde.", ""},
        {"Chaque chose en son temps.", ""},
        {"Aux grands maux les grands remèdes.", ""},
        {"Il vaut mieux faire envie que pitié.", ""},
        {"Un homme averti en vaut deux.", ""},
        {"Les jours se suivent et ne se ressemblent pas.", ""},
        {"À chacun son métier, et les vaches seront bien gardées.", ""},
        {"Il n'y a que le premier pas qui coûte.", ""},
        {"Le ridicule ne tue pas.", ""},
        {"La vérité sort de la bouche des enfants.", ""},
        {"Qui ne dit mot consent.", ""},
        {"Faute de grives, on mange des merles.", ""},
        {"Fais ce que dois, advienne que pourra.", ""},
        {"Il ne faut pas mettre la charrue avant les bœufs.", ""},
        {"Il ne faut pas mettre tous ses œufs dans le même panier.", ""},
        {"Avec des si, on mettrait Paris en bouteille.", ""},
        {"Le bonheur des uns fait le malheur des autres.", ""},
        {"Qui casse les verres les paie.", ""},
        {"Les cordonniers sont toujours les plus mal chaussés.", ""},
        {"C'est dans le besoin qu'on reconnaît ses vrais amis.", ""},
        {"L'occasion fait le larron.", ""},
        {"Tout ce qui brille n'est pas or.", ""},
        {"Un malheur n'arrive jamais seul.", ""},
        {"Il n'y a pas de fumée sans feu.", ""},
        {"Chose promise, chose due.", ""},
        {"Mieux vaut tard que jamais.", ""},
        {"L'habit ne fait pas le moine.", ""},
        {"Qui sème le vent récolte la tempête.", ""},
        {"Qui se ressemble s'assemble.", ""},
        {"Pierre qui roule n'amasse pas mousse.", ""},
        {"Il faut battre le fer pendant qu'il est chaud.", ""},
        {"Tel père, tel fils.", ""},
        {"Chacun voit midi à sa porte.", ""},
        {"Il ne faut jamais dire : « Fontaine, je ne boirai pas de ton eau. »", ""},
        {"Qui dort dîne.", ""},
        {"Le temps, c'est de l'argent.", ""},
        {"Les murs ont des oreilles.", ""},
        {"Il y a loin de la coupe aux lèvres.", ""},
        {"Loin des yeux, loin du cœur.", ""},
        {"Le jeu n'en vaut pas la chandelle.", ""},
        {"Il faut que jeunesse se passe.", ""},
        {"Charité bien ordonnée commence par soi-même.", ""},
        {"Abondance de biens ne nuit pas.", ""},
        {"Ce qui est fait n'est plus à faire.", ""},
        {"Bon sang ne saurait mentir.", ""},
        {"Rien de nouveau sous le soleil.", ""},
    };

    /** Today's quote: {text, author}, the same all day, a different one each day of the year. */
    static String[] quote(long now) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        int day = c.get(Calendar.YEAR) * 366 + c.get(Calendar.DAY_OF_YEAR);
        return QUOTES[Math.floorMod(day * 37, QUOTES.length)];
    }

    // MARK: - Age

    /** Age on `now` from a "yyyy-MM-dd" birth date: {years, months, days, total days}, or null if unset or in the future. */
    static int[] age(String birth, long now) {
        if (birth == null || !birth.matches("\\d{4}-\\d{2}-\\d{2}")) return null;
        Calendar b = Calendar.getInstance();
        b.clear();
        b.set(Integer.parseInt(birth.substring(0, 4)), Integer.parseInt(birth.substring(5, 7)) - 1, Integer.parseInt(birth.substring(8, 10)));
        Calendar t = Calendar.getInstance();
        t.setTimeInMillis(now);
        Calendar today = Calendar.getInstance();
        today.clear();
        today.set(t.get(Calendar.YEAR), t.get(Calendar.MONTH), t.get(Calendar.DAY_OF_MONTH));
        if (today.before(b)) return null;
        int months = (today.get(Calendar.YEAR) - b.get(Calendar.YEAR)) * 12 + today.get(Calendar.MONTH) - b.get(Calendar.MONTH);
        Calendar mark = (Calendar) b.clone();
        mark.add(Calendar.MONTH, months);
        if (mark.after(today)) {
            months--;
            mark = (Calendar) b.clone();
            mark.add(Calendar.MONTH, months);
        }
        int days = (int) Math.round((today.getTimeInMillis() - mark.getTimeInMillis()) / 86_400_000.0);
        int total = (int) Math.round((today.getTimeInMillis() - b.getTimeInMillis()) / 86_400_000.0);
        return new int[] {months / 12, months % 12, days, total};
    }

    /** "5 months and 12 days", "1 year and 3 months", "12 days": what follows "Inès is". */
    static String ageText(int[] a) {
        int years = a[0], months = a[1], days = a[2];
        if (years >= 2) return plural(years, "ano", "year", "año") + (months > 0 ? and() + plural(months, "mês", "month", "mes") : "");
        int allMonths = years * 12 + months;
        if (allMonths == 0) return plural(days, "dia", "day", "día");
        if (years == 1 && months == 0) return plural(1, "ano", "year", "año") + (days > 0 ? and() + plural(days, "dia", "day", "día") : "");
        return plural(allMonths, "mês", "month", "mes") + (days > 0 ? and() + plural(days, "dia", "day", "día") : "");
    }

    /** Today's milestone, or "": a birthday, a month, the 100th day… */
    static String milestone(int[] a) {
        int years = a[0], months = a[1], days = a[2], total = a[3];
        if (total == 0) return Ui.tr("Bem-vindo ao mundo!", "Welcome to the world!", "¡Bienvenido al mundo!");
        if (days == 0 && months == 0) return Ui.tr("Feliz aniversário!", "Happy birthday!", "¡Feliz cumpleaños!");
        if (days == 0 && years == 0) return Ui.tr("Hoje: ", "Today: ", "Hoy: ") + plural(months, "mês", "month", "mes") + " !".substring(Ui.fr ? 0 : 1);
        if (total == 100 || total == 200 || total == 500 || total == 1000) {
            if (Ui.fr) return "Aujourd'hui : son " + total + "e jour !";
            return Ui.tr("Hoje: o dia ", "Today: day ", "Hoy: el día ") + total + "!";
        }
        return "";
    }

    private static String and() {
        return Ui.tr(" e ", " and ", " y ");
    }

    private static String plural(int n, String pt, String en, String es) {
        String word;
        if (Ui.fr) word = en.equals("year") ? "an" : en.equals("month") ? "mois" : "jour";
        else if (Ui.de) word = en.equals("year") ? "Jahr" : en.equals("month") ? "Monat" : "Tag";
        else if (Ui.ru) word = en.equals("year") ? "г." : en.equals("month") ? "мес." : "дн.";
        else if (Ui.es) word = es;
        else if (Ui.en) word = en;
        else word = pt;
        if (n != 1 && !Ui.ru) {
            if (Ui.fr) word = word.equals("mois") ? word : word + "s";
            else if (Ui.de) word = word.equals("Tag") ? "Tage" : word + "e";
            else if (word.equals("mês")) word = "meses";
            else if (word.equals("mes")) word = "meses";
            else word = word + "s";
        }
        return n + " " + word;
    }

    /** "Inès is 5 months and 12 days old" as one line (the card shows it with the name in bold). */
    static String ageLine(String name, int[] a) {
        String age = ageText(a);
        if (Ui.fr) return name + " a " + age;
        if (Ui.de) return name + " ist " + age + " alt";
        if (Ui.ru) return name + ": " + age;
        if (Ui.es) return name + " tiene " + age;
        if (Ui.en) return name + " is " + age + " old";
        return name + " tem " + age;
    }

    /** "3 May 2026" for Home Settings. */
    static String birthLabel(String birth) {
        if (birth == null || !birth.matches("\\d{4}-\\d{2}-\\d{2}")) return "";
        Calendar b = Calendar.getInstance();
        b.clear();
        b.set(Integer.parseInt(birth.substring(0, 4)), Integer.parseInt(birth.substring(5, 7)) - 1, Integer.parseInt(birth.substring(8, 10)));
        return new java.text.SimpleDateFormat("d MMM yyyy", Ui.locale()).format(b.getTime());
    }

    /** "03/05/2026", "3.5.2026" or "2026-05-03" typed on the TV → "2026-05-03", or null. Day first, as in Europe. */
    static String parseBirth(String typed) {
        String s = typed == null ? "" : typed.trim();
        java.util.regex.Matcher iso = java.util.regex.Pattern.compile("^(\\d{4})[-/.](\\d{1,2})[-/.](\\d{1,2})$").matcher(s);
        java.util.regex.Matcher dmy = java.util.regex.Pattern.compile("^(\\d{1,2})[-/. ](\\d{1,2})[-/. ](\\d{4})$").matcher(s);
        int y, m, d;
        if (iso.matches()) {
            y = Integer.parseInt(iso.group(1)); m = Integer.parseInt(iso.group(2)); d = Integer.parseInt(iso.group(3));
        } else if (dmy.matches()) {
            d = Integer.parseInt(dmy.group(1)); m = Integer.parseInt(dmy.group(2)); y = Integer.parseInt(dmy.group(3));
        } else {
            return null;
        }
        if (m < 1 || m > 12 || d < 1 || d > 31 || y < 1900) return null;
        Calendar c = Calendar.getInstance();
        c.setLenient(false);
        c.clear();
        try {
            c.set(y, m - 1, d);
            c.getTimeInMillis();   // throws on 31/02
        } catch (IllegalArgumentException e) {
            return null;
        }
        return String.format(Locale.US, "%04d-%02d-%02d", y, m, d);
    }
}
