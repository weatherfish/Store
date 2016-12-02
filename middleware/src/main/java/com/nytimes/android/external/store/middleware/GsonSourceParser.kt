package com.nytimes.android.external.store.middleware

import com.google.gson.Gson
import com.nytimes.android.external.store.base.Parser

import java.io.IOException
import java.io.InputStreamReader

import javax.inject.Inject

import okio.BufferedSource

/**
 * Parser to be used when going from a BufferedSource to any Parsed Type
 * example usage:
 * ParsingStoreBuilder.<BufferedSource></BufferedSource>, BookResults>builder()
 * .fetcher(fetcher)
 * .persister(new SourcePersister(fileSystem))
 * .parser(new GsonSourceParser<>(gson, BookResults.class))
 * .open();
 */


class GsonSourceParser<Parsed>
@Inject
constructor(private val gson: Gson, private val parsedClass: Class<Parsed>) : Parser<BufferedSource, Parsed> {

    override fun call(source: BufferedSource): Parsed {
        try {
            InputStreamReader(source.inputStream()).use {
                reader -> return gson.fromJson<Parsed>(reader, parsedClass)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }
}

inline fun <reified Parsed> GsonSourceParser(gson: Gson) =
    GsonSourceParser(gson, Parser::class.java)
