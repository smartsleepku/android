package dk.ku.sund.smartsleep.manager

import android.content.Context
import dk.ku.sund.smartsleep.model.Configuration
import kotlinx.coroutines.sync.Mutex
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception

val volatileStore = mutableMapOf<String,Any>()

private var store: File? = null
private var initialized = false

fun store(key: String, value: String) {
    FileOutputStream(File(store, "${key}.json")).use {
        it.write(value.toByteArray())
        it
    }.close()
}

fun store(key: String, value: Any) {
    store(key, gson.toJson(value))
}

fun <T> load(key: String, klass: Class<T>): T? =
    try {
        val reader = FileInputStream(File(store, "${key}.json")).bufferedReader()
        if (klass.simpleName == "String") {
            val value = reader.use { it.readText() }
            reader.close()
            value as T
        } else {
            val value: T = gson.fromJson(
                reader.use { it.readText() },
                klass)
            reader.close()
            value
        }
    } catch (e: Exception) {
        null
    }

fun initializeStore(context: Context) {
    if (initialized) return
    initialized = true
    store = File(context.getFilesDir(), "store")
    store?.mkdirs()
}