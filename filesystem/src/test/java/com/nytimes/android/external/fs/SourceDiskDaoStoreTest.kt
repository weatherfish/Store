package com.nytimes.android.external.fs


import com.google.gson.Gson
import com.nytimes.android.external.store.base.Fetcher
import com.nytimes.android.external.store.base.Store
import com.nytimes.android.external.store.base.impl.BarCode
import com.nytimes.android.external.store.base.impl.ParsingStoreBuilder
import com.nytimes.android.external.store.middleware.GsonSourceParser

import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

import java.io.ByteArrayInputStream

import okio.BufferedSource
import okio.Okio
import rx.Observable

import com.google.common.base.Charsets.UTF_8
import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class SourceDiskDaoStoreTest {
    @Mock
    internal var fetcher: Fetcher<BufferedSource>? = null
    @Mock
    internal var diskDAO: SourcePersister? = null

    private val barCode = BarCode("value", KEY)

    @Test
    fun testSimple() {
        MockitoAnnotations.initMocks(this)
        //val parser =
        val simpleStore = ParsingStoreBuilder.builder<BufferedSource, Foo>()
                .persister(diskDAO!!)
                .fetcher(fetcher!!)
                .parser(GsonSourceParser(Gson()))
                .open()


        val foo = Foo()
        foo.bar = barCode.key

        val sourceData = Gson().toJson(foo)


        val source = source(sourceData)
        val value = Observable.just(source)
        `when`(fetcher!!.fetch(barCode))
                .thenReturn(value)

        `when`(diskDAO!!.read(barCode))
                .thenReturn(Observable.empty<BufferedSource>())
                .thenReturn(value)

        `when`(diskDAO!!.write(barCode, source))
                .thenReturn(Observable.just(true))

        var result = simpleStore.get(barCode).toBlocking().first()
        assertThat(result.bar).isEqualTo(KEY)
        result = simpleStore.get(barCode).toBlocking().first()
        assertThat(result.bar).isEqualTo(KEY)
        verify<Fetcher<BufferedSource>>(fetcher, times(1)).fetch(barCode)
    }

    private class Foo internal constructor() {
        internal var bar: String? = null
    }

    companion object {
        val KEY = "key"

        private fun source(data: String): BufferedSource {
            return Okio.buffer(Okio.source(ByteArrayInputStream(data.toByteArray(UTF_8))))
        }
    }

}
