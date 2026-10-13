package com.canbustool.app.state

import com.canbustool.core.CanMessage
import com.canbustool.core.Direction

/** A message as it appears in the unified Trace log, tagged with which way it went. */
data class DirectedMessage(val message: CanMessage, val direction: Direction)
