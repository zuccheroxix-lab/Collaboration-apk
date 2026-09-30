package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.GameDatabase
import com.example.data.GameProfileEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: GameDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, GameDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Sensiv Game Tools", appName)
    }

    @Test
    fun testRoomGameProfilePersistence() = runBlocking {
        val dao = db.gameProfileDao()
        val profile = GameProfileEntity(
            packageName = "com.dts.freefireth",
            gameName = "Free Fire",
            sensX = 1.45f,
            sensY = 1.20f,
            isLinked = false,
            linkRatio = 0.83f,
            preset = "CUSTOM",
            pointerSpeed = 3,
            targetDpi = 360,
            useOverlay = true,
            preferredMode = "PERFORMANCE"
        )

        dao.upsertProfile(profile)
        val loaded = dao.getProfileSync("com.dts.freefireth")

        assertNotNull(loaded)
        assertEquals("Free Fire", loaded?.gameName)
        assertEquals(1.45f, loaded?.sensX ?: 0f, 0.001f)
        assertEquals(1.20f, loaded?.sensY ?: 0f, 0.001f)
        assertEquals(360, loaded?.targetDpi)
        assertEquals("PERFORMANCE", loaded?.preferredMode)
    }
}
