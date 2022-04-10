package com.github.whyrising.recompose.interceptor

import com.github.whyrising.recompose.events.Event
import com.github.whyrising.recompose.schemas.CoeffectsSchema
import com.github.whyrising.recompose.schemas.ContextSchema
import com.github.whyrising.recompose.schemas.ContextSchema.coeffects
import com.github.whyrising.recompose.schemas.ContextSchema.queue
import com.github.whyrising.recompose.schemas.ContextSchema.stack
import com.github.whyrising.recompose.schemas.InterceptorSchema
import com.github.whyrising.y.assocIn
import com.github.whyrising.y.collections.map.IPersistentMap
import com.github.whyrising.y.collections.seq.ISeq
import com.github.whyrising.y.conj
import com.github.whyrising.y.get
import com.github.whyrising.y.l
import com.github.whyrising.y.m

typealias Context = IPersistentMap<ContextSchema, Any>

typealias Interceptor = IPersistentMap<InterceptorSchema, Any>

typealias InterceptorFn = suspend (context: Context) -> Context

internal val defaultInterceptorFn: InterceptorFn = { it }

fun toInterceptor(
    id: Any,
    before: InterceptorFn = defaultInterceptorFn,
    after: InterceptorFn = defaultInterceptorFn
): Interceptor = m(
    InterceptorSchema.id to id,
    InterceptorSchema.before to before,
    InterceptorSchema.after to after,
)

fun assocCofx(
    context: Context,
    key: CoeffectsSchema,
    value: Any
): Context = assocIn(context, l(coeffects, key), value) as Context

internal fun enqueue(
    context: Context,
    interceptors: ISeq<Interceptor>?
): Context = context.assoc(queue, interceptors ?: l<Any>())

/**
 * Create a fresh context.
 */
internal fun context(
    event: Event,
    interceptors: ISeq<Interceptor>
): Context = m<ContextSchema, Any>()
    .let { assocCofx(it, CoeffectsSchema.event, event) }
    .let { assocCofx(it, CoeffectsSchema.originalEvent, event) }
    .let { enqueue(it, interceptors) }

// -- Execute Interceptor Chain  ----------------------------------------------

@Suppress("UNCHECKED_CAST")
internal suspend fun invokeInterceptorFn(
    context: Context,
    interceptor: Interceptor,
    direction: InterceptorSchema
) = when (val interceptorFn = interceptor[direction] as InterceptorFn?) {
    null -> context
    else -> interceptorFn(context)
}

/**
 * :queue and :stack in context should be lists/interceptors of type
 * IPersistentVector<*>.
 */
@Suppress("UNCHECKED_CAST")
internal suspend fun invokeInterceptors(
    context: Context,
    direction: InterceptorSchema
): Context {
    tailrec suspend fun invokeInterceptors(context: Context): Context {
        val que = context[queue] as ISeq<Interceptor>
        return when (que.count) {
            0 -> context
            else -> {
                val interceptor = que.first()
                val stk = context[stack] as ISeq<Any>?
                val newContext = context
                    .assoc(queue, que.rest())
                    .assoc(stack, conj(stk, interceptor))
                    .let { invokeInterceptorFn(it, interceptor, direction) }

                invokeInterceptors(newContext)
            }
        }
    }

    return invokeInterceptors(context)
}

@Suppress("UNCHECKED_CAST")
internal fun changeDirection(context: Context): Context =
    enqueue(context, context[stack] as ISeq<Interceptor>?)

suspend fun execute(
    event: Event,
    interceptors: ISeq<Interceptor>
): Context = context(event, interceptors)
    .let { invokeInterceptors(it, InterceptorSchema.before) }
    .let { changeDirection(it) }
    .let { invokeInterceptors(it, InterceptorSchema.after) }
