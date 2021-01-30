package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.Validated

typealias ConfigResult<A> = Validated<ConfigFailure, A>

