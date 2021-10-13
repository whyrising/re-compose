package com.github.whyrising.recompose.schemas

@Suppress("EnumEntryName")
enum class Schema {
    event,
    originalEvent,
    db,
    fx,
    dispatch,
    dispatchN,
    dofx,
    notFound;

    override fun toString(): String = ":${super.toString()}"
}
