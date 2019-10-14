package com.sksamuel.hoplite

import arrow.core.Validated

typealias ConfigResult<A> = Validated<ConfigFailure, A>

