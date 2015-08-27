package org.zenframework.z8.server.locale;

import java.util.StringTokenizer;

import org.zenframework.z8.server.resources.Resources;

public class Locale {
    private String language = Resources.DefaultLanguage;
    private String decimalDelimiter = ".";
    private String numberDelimiter = " ";
    private String dateDelimiter = "/";
    private String timeDelimiter = ":";

    /*
     * Конструктор по- умолчанию создает locale русский - дефолтный для системы
     */
    public Locale() {}

    /*
     * Структура строки с locale |язык|десятичный разделитель|разделитель
     * разрядов|разделитель даты|разделитель времени|
     */
    public Locale(String locale) {
        StringTokenizer tLocale = new StringTokenizer(locale, "|");

        /*
         * Выберем язык из имеющихся ресурсов, если не нашли язык или не пришла
         * информация о языке клиента, то выбираеем русский по - умолчанию
         */
        if(tLocale.hasMoreTokens()) {
            String lang = tLocale.nextToken();
            StringTokenizer t = new StringTokenizer(lang, ",");

            boolean loaded = false;

            while(t.hasMoreTokens()) {
                String element = t.nextToken();
                StringTokenizer tLang = new StringTokenizer(element, "-");

                while(tLang.hasMoreTokens()) {
                    language = tLang.nextToken();

                    if(Resources.getResources().load(language)) {
                        loaded = true;
                        break;
                    }
                }

                if(loaded) {
                    break;
                }
            }

            if(!loaded) {
                language = Resources.DefaultLanguage;
            }
        }

        // Десятичный разделитель
        if(tLocale.hasMoreTokens())
            decimalDelimiter = tLocale.nextToken();

        // Разделитель разрядов
        if(tLocale.hasMoreTokens())
            numberDelimiter = tLocale.nextToken();

        // Разделитель даты
        if(tLocale.hasMoreTokens())
            dateDelimiter = tLocale.nextToken();

        // Разделитель времени
        if(tLocale.hasMoreTokens())
            timeDelimiter = tLocale.nextToken();
    }

    public String getUserLanguage() {
        return language;
    }

    public String getDecimalDelimiter() {
        return decimalDelimiter;
    }

    public String getNumberDelimiter() {
        return numberDelimiter;
    }

    public String getDateDelimiter() {
        return dateDelimiter;
    }

    public String getTimeDelimiter() {
        return timeDelimiter;
    }
}
