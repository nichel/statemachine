package org.nichel.sm.annotation

import android.support.annotation.StringRes

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class OnPerformAction(@StringRes val enter: String, @StringRes val exit: String)
