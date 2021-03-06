package com.github.whyrising.recompose.example.events

import com.github.whyrising.recompose.cofx.Coeffects
import com.github.whyrising.recompose.cofx.injectCofx
import com.github.whyrising.recompose.events.Event
import com.github.whyrising.recompose.example.Ids
import com.github.whyrising.recompose.example.Ids.exitApp
import com.github.whyrising.recompose.example.Ids.nextTick
import com.github.whyrising.recompose.example.Ids.setPrimaryColor
import com.github.whyrising.recompose.example.Ids.setSecondaryColor
import com.github.whyrising.recompose.example.Ids.startTicking
import com.github.whyrising.recompose.example.Ids.ticker
import com.github.whyrising.recompose.example.db.AppDb
import com.github.whyrising.recompose.fx.FxIds.fx
import com.github.whyrising.recompose.ids.recompose.db
import com.github.whyrising.recompose.regEventDb
import com.github.whyrising.recompose.regEventFx
import com.github.whyrising.y.core.get
import com.github.whyrising.y.core.m
import com.github.whyrising.y.core.v
import java.util.Date

fun regAllEvents() {
  regEventFx(
    id = nextTick,
    interceptors = v(injectCofx(Ids.now)),
  ) { cofx, _ ->
    val appDb = cofx[db] as AppDb
    m(db to appDb.copy(time = cofx[Ids.now] as Date))
  }

  regEventFx(id = startTicking) { _, _ ->
    m(fx to v(v(ticker, null), null))
  }

  regEventDb<AppDb>(id = setPrimaryColor) { db, (_, colorInput) ->
    db.copy(primaryColor = colorInput as String)
  }

  regEventDb<AppDb>(id = setSecondaryColor) { db, (_, colorInput) ->
    db.copy(secondaryColor = colorInput as String)
  }

  regEventFx(exitApp) { _: Coeffects, _: Event ->
    m(fx to v(v(exitApp, null)))
  }
}
