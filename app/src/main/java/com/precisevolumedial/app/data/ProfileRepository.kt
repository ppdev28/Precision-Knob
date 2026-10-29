package com.precisevolumedial.app.data

import android.content.Context
import com.precisevolumedial.app.audio.eq.EqProfile
import com.precisevolumedial.app.audio.eq.ParametricEqParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Headphone profiles saved on the device (small JSON file in app storage). */
class ProfileRepository(context: Context) {
    private val file = File(context.filesDir, "eq_profiles.json")
    private val _profiles = MutableStateFlow(load())
    val profiles: StateFlow<List<EqProfile>> = _profiles

    /** Parses [rawText] and saves it, replacing an existing profile with the same name and source. Throws if invalid. */
    @Synchronized
    fun add(name: String, source: String?, rawText: String): EqProfile {
        val parsed = ParametricEqParser.parse(rawText)
        val profile = EqProfile(
            id = slug("$name ${source.orEmpty()}"),
            name = name,
            source = source,
            preampDb = parsed.preampDb,
            filters = parsed.filters,
            raw = rawText,
        )
        _profiles.value = _profiles.value.filterNot { it.id == profile.id } + profile
        save()
        return profile
    }

    @Synchronized
    fun remove(id: String) {
        _profiles.value = _profiles.value.filterNot { it.id == id }
        save()
    }

    private fun save() {
        val array = JSONArray()
        _profiles.value.forEach {
            array.put(JSONObject().put("name", it.name).put("source", it.source ?: JSONObject.NULL).put("raw", it.raw))
        }
        file.writeText(array.toString())
    }

    private fun load(): List<EqProfile> = runCatching {
        val array = JSONArray(file.readText())
        (0 until array.length()).mapNotNull { i ->
            val o = array.getJSONObject(i)
            val source = if (o.isNull("source")) null else o.getString("source")
            val raw = o.getString("raw")
            runCatching {
                val parsed = ParametricEqParser.parse(raw)
                val name = o.getString("name")
                EqProfile(slug("$name ${source.orEmpty()}"), name, source, parsed.preampDb, parsed.filters, raw)
            }.getOrNull()
        }
    }.getOrDefault(emptyList())

    private fun slug(text: String) = text.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
}
