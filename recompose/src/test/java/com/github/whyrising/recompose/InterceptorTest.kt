package com.github.whyrising.recompose

import com.github.whyrising.recompose.Framework.before
import com.github.whyrising.recompose.Framework.coeffects
import com.github.whyrising.recompose.Framework.db
import com.github.whyrising.recompose.Framework.event
import com.github.whyrising.recompose.Framework.originalEvent
import com.github.whyrising.recompose.Framework.queue
import com.github.whyrising.recompose.Framework.stack
import com.github.whyrising.recompose.interceptor.changeDirection
import com.github.whyrising.recompose.interceptor.context
import com.github.whyrising.recompose.interceptor.invokeInterceptorFn
import com.github.whyrising.recompose.interceptor.invokeInterceptors
import com.github.whyrising.recompose.interceptor.toInterceptor
import com.github.whyrising.y.collections.concretions.list.PersistentList
import com.github.whyrising.y.collections.core.get
import com.github.whyrising.y.collections.core.l
import com.github.whyrising.y.collections.core.m
import com.github.whyrising.y.collections.core.v
import com.github.whyrising.y.collections.map.IPersistentMap
import com.github.whyrising.y.collections.vector.IPersistentVector
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

class InterceptorTest : FreeSpec({
    "context(event, interceptors) should return a fresh context" {
        val eventVec = v<Any>(":id", 12)
        val interceptors = l<IPersistentMap<Framework, Any>>()

        val context = context(eventVec, interceptors)

        context shouldBe m(
            coeffects to m(
                event to eventVec,
                originalEvent to eventVec
            ),
            queue to interceptors
        )
    }

    "changeDirection(context) should put the stack into a new the queue" {
        val context = m<Framework, Any>(
            queue to v<Any>(),
            stack to v(1, 2, 3)
        )

        val c = changeDirection(context)

        c shouldBe m<Framework, Any>(
            queue to v<Any>(1, 2, 3),
            stack to v(1, 2, 3)
        )
    }

    "invokeInterceptorFn() should call the interceptor fun based direction" - {
        val context0 = m<Framework, Any>(
            queue to v<Any>(),
            stack to v(1, 2, 3)
        )

        val f: suspend (
            IPersistentMap<Framework, Any>
        ) -> IPersistentMap<Framework, Any> = { context ->
            val q = (context[queue] as IPersistentVector<Any>).conj(1)
            context.assoc(queue, q)
        }

        val g: suspend (
            IPersistentMap<Framework, Any>
        ) -> IPersistentMap<Framework, Any> = { context ->
            val q = (context[queue] as IPersistentVector<Any>).plus(1)
            context.assoc(queue, q)
        }

        val addToQAfter = toInterceptor(
            id = ":add-to-queue",
            after = f
        )

        "should call :before and add to the context" {
            val addToQ = toInterceptor(id = ":add-to-queue", before = g)

            val context = invokeInterceptorFn(context0, addToQ, before)

            context shouldBe m<Framework, Any>(
                queue to v(1),
                stack to v(1, 2, 3)
            )
        }

        "should call :after and add to the context" {
            val context =
                invokeInterceptorFn(context0, addToQAfter, Framework.after)

            context shouldBe m<Framework, Any>(
                queue to v(1),
                stack to v(1, 2, 3)
            )
        }

        "when some direction set to default, should return the same context" {
            val context = invokeInterceptorFn(context0, addToQAfter, before)

            context shouldBeSameInstanceAs context0
        }
    }

    "invokeInterceptors(context)" - {
        "should return the same given context when the :queue is empty" {
            val context = m<Framework, Any>(
                queue to l<Any>(),
                stack to l<Any>()
            )

            val newContext = invokeInterceptors(context, before)

            newContext shouldBeSameInstanceAs context
        }

        """
            It should make a new context by invoking all interceptors in :queue
            and stack then in :stack while emptying the queue
        """ {
            val f1: suspend (
                IPersistentMap<Framework, Any>
            ) -> IPersistentMap<Framework, Any> = { context ->
                context.assoc(db, (context[db] as Int).inc())
            }

            val f2: suspend (
                IPersistentMap<Framework, Any>
            ) -> IPersistentMap<Framework, Any> = { context ->
                context.assoc(db, (context[db] as Int) + 2)
            }

            val incBy1 = toInterceptor(id = ":incBy1", before = f1)
            val incBy2 = toInterceptor(id = ":incBy2", before = f2)

            val qu = l<Any>(incBy1, incBy2)
            val stck = l<Any>()

            val context = m(
                db to 0,
                queue to qu,
                stack to stck
            )

            val newContext = invokeInterceptors(context, before)

            newContext[db] as Int shouldBeExactly 3
            (newContext[queue] as PersistentList<*>).shouldBeEmpty()
            (newContext[stack] as PersistentList<*>) shouldContainExactly
                qu.reversed()
        }
    }

    "changeDirection(context) should fill the queue from the stack" {
        val s = v<Any>(1, 2, 3)

        val context = m(
            queue to v(),
            stack to s
        )

        val newContext = changeDirection(context)

        (newContext[queue] as IPersistentVector<*>) shouldContainExactly s
        (newContext[stack] as IPersistentVector<*>) shouldContainExactly s
    }
})
