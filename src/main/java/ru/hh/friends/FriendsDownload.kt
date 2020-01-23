package ru.hh.friends

import org.jsoup.Jsoup


val FRIENDS_URL = "https://fangj.github.io/friends/"


fun getSpeechList(): ArrayList<Speech> {
    val speeches = ArrayList<Speech>()

    Jsoup.connect(FRIENDS_URL).get().run {
        var season = ""
        this.body().children().forEachIndexed { index, element ->
            if (element.tagName() == "ul") {
                element.select("a").forEachIndexed { index, element ->
                    val series = element.text()
                    val link = FRIENDS_URL + element.attr("href")
                    println("$season $series $link")
                    Jsoup.connect(link).get().run {
                        val doc = this
                        if (doc.body().select("p").count() > 5) {
                            doc.body().select("p").forEachIndexed { index, element ->
                                val line = element.text()
                                val speech = createSpeech(line, season, series, link)
                                if (speech != null) {
                                    speeches.add(speech)
                                }
                            }
                        } else {
                            doc.body().select("p[align=left]").html().split("<br> <br>").forEach {
                                val line = it.replace(Regex("<[^>]*>"), "").trim()
                                val speech = createSpeech(line, season, series, link)
                                if (speech != null) {
                                    speeches.add(speech)
                                }
                            }
                        }
                    }
                }
            } else if (element.tagName() == "h3") {
                season = element.text()
            }
        }
    }

    println(": "+speeches.size)
    return speeches
}

fun createSpeech(line: String, season: String, series: String, link: String): Speech? {
    if (line.isNotEmpty() && line.isNotBlank()
            && line[0] != '[' && line[0] != '('
            && !line.startsWith("Written by:")
            && !line.startsWith("Commercial Break")
            && !line.startsWith("End")
            && !line.startsWith("THE END")
            && line.contains(':')
    ) {
        val s = line.split(":")
        val character = s[0].toLowerCase()
        val phrase = s[1].toLowerCase()
        val seriesNumberString  = series.split(" ")[0]
        val seriesNumber = if (seriesNumberString.contains("-")) {
            seriesNumberString.split("-")[0].toInt()
        } else {
            seriesNumberString.toInt()
        }
        val seasonNumber = season.split(" ")[1].toInt()

        return Speech(link, season, series, character, phrase, seasonNumber, seriesNumber)
    } else {
        return null
    }
}