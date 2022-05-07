package click

case class ClickConfig(
                      MUX_DELAY: Int = 3,
                      DEMUX_DELAY: Int = 3,
                      REG_DELAY: Int = 5,
                      ADD_DELAY: Int = 15,
                      MERGE_DELAY: Int = 2,
                      JOIN_DELAY: Int = 2,
                      FORK_DELAY: Int = 2,
                      SIMULATION: Boolean = true
                      )

//case class DefaultClickConfig() extends ClickConfig {
//
//}

//object ClickConfig {
//  val ADD_DELAY = 15
//
//  var SIMULATION = true
//}
