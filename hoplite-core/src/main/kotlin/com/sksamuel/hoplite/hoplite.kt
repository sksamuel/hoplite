package com.sksamuel.hoplite

import arrow.data.Validated

typealias ConfigResult<A> = Validated<ConfigFailure, A>

