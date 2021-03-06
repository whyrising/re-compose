package com.github.whyrising.recompose

import com.github.whyrising.recompose.registrar.Kinds.Cofx
import com.github.whyrising.recompose.registrar.Kinds.Event
import com.github.whyrising.recompose.registrar.Kinds.Fx
import com.github.whyrising.recompose.registrar.Kinds.Sub
import com.github.whyrising.recompose.registrar.getHandler
import com.github.whyrising.recompose.registrar.registerHandler
import com.github.whyrising.y.core.collections.IPersistentVector
import com.github.whyrising.y.core.m
import com.github.whyrising.y.core.v
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import com.github.whyrising.recompose.registrar.register as myRegister

class RegistrarTest : FreeSpec({
  afterTest {
    myRegister.reset(m())
  }

  "registerHandler()/getHandler(kind)" - {
    "Fx kind" {
      val id = ":fx"
      val handlerFn = { _: Any -> }
      val hFn = registerHandler(id, Fx, handlerFn)

      val handler = getHandler(Fx, id)

      handler shouldBeSameInstanceAs handlerFn
      hFn shouldBeSameInstanceAs handlerFn
    }

    "Event kind" {
      val id = ":event"
      val interceptors = v<Any>()
      registerHandler(id, Event, interceptors)

      val handler = getHandler(Event, id)

      handler shouldBeSameInstanceAs interceptors
    }

    "Cofx kind" {
      val id = ":cofx"
      val handlerFn: (Any) -> Any = { _ -> }
      registerHandler(id, Cofx, handlerFn)

      val handler = getHandler(Cofx, id)

      myRegister().count shouldBeExactly 1
      handler shouldBeSameInstanceAs handlerFn
    }

    "Sub kind" {
      val id = ":sub"
      val handlerFn: (Any, IPersistentVector<Any>) -> Any = { _, _ -> }
      registerHandler(id, Sub, handlerFn)

      val handler = getHandler(Sub, id)

      handler shouldBeSameInstanceAs handlerFn
    }

    "two kinds of handlers with same id should register separately" {
      val id = ":id"
      val handlerFn1 = { _: Any -> }
      val handlerFn2 = { _: Any -> }
      val handlerFn3 = { _: Any -> }
      val handlerFn4 = { _: Any -> }
      registerHandler(id, Fx, handlerFn1)
      registerHandler(id, Cofx, handlerFn2)
      registerHandler(id, Event, handlerFn3)
      registerHandler(id, Sub, handlerFn4)

      val handler1 = getHandler(Fx, id)
      val handler2 = getHandler(Cofx, id)
      val handler3 = getHandler(Event, id)
      val handler4 = getHandler(Sub, id)

      handler1 shouldBeSameInstanceAs handlerFn1
      handler2 shouldBeSameInstanceAs handlerFn2
      handler3 shouldBeSameInstanceAs handlerFn3
      handler4 shouldBeSameInstanceAs handlerFn4

      handler1 shouldNotBeSameInstanceAs handler2
      handler2 shouldNotBeSameInstanceAs handler3
      handler3 shouldNotBeSameInstanceAs handler4
      handler4 shouldNotBeSameInstanceAs handler1
    }
  }
})
