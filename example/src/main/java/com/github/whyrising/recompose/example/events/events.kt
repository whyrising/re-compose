package com.github.whyrising.recompose.example.events

import com.github.whyrising.recompose.cofx.injectCofx
import com.github.whyrising.recompose.dispatchSync
import com.github.whyrising.recompose.example.Ids
import com.github.whyrising.recompose.example.Ids.initDb
import com.github.whyrising.recompose.example.Ids.setPrimaryColor
import com.github.whyrising.recompose.example.Ids.startTicking
import com.github.whyrising.recompose.example.Ids.ticktack
import com.github.whyrising.recompose.example.db.AppDb
import com.github.whyrising.recompose.fx.FxIds.fx
import com.github.whyrising.recompose.regEventDb
import com.github.whyrising.recompose.regEventFx
import com.github.whyrising.recompose.schemas.Schema.db
import com.github.whyrising.y.get
import com.github.whyrising.y.m
import com.github.whyrising.y.v
import java.util.Date

fun regAllEvents() {
    regEventDb<Any>(id = initDb) { _, _ -> AppDb() }
    dispatchSync(v(initDb))

    regEventFx(
        id = ticktack,
        interceptors = v(injectCofx(Ids.now)),
    ) { cofx, _ ->
        val appDb = cofx[db] as AppDb
        m(db to appDb.copy(time = cofx[Ids.now] as Date))
    }

    regEventFx(id = startTicking) { _, _ ->
        m(fx to v(v(ticktack)))
    }

    regEventDb<AppDb>(id = setPrimaryColor) { db, (_, colorInput) ->
        db.copy(primaryColor = colorInput as String)
    }

    regEventDb<AppDb>(id = Ids.setSecondaryColor) { db, (_, colorInput) ->
        db.copy(secondaryColor = colorInput as String)
    }
}