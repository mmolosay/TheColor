package com.ordolabs.util

inline fun <reified I> List<*>.firstOf(): I? {
    return this.firstOrNull { it is I } as I?
}