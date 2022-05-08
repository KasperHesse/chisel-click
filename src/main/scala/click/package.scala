import chisel3._

package object click {

  /**
   * Generates a simulation delay, useful for debugging wave traces
   * When synthesizing and [[ClickConfig.SIMULATION]] is false, simply connects the input to the output
   * @param reqIn The input signal that should be delayed
   * @param delay The default delay value from the configuration used.
   * @param conf The configuration object in use
   * @return
   */
  def simDelay(reqIn: Bool, delay: Int)(implicit conf: ClickConfig): Bool = {
    if(delay > 0 && conf.SIMULATION) {
      val d = Module(new DelayElementSim(delay))
      d.io.reqIn := reqIn
      d.io.reqOut
    } else {
      //No delay
      reqIn
    }
  }

  /**
   * Generates a synthesizable delay element, for delaying request signals
   * until their attached combinational logic has finished evaluating.
   * If [[ClickConfig.SIMULATION]] is true, instantiates a simulation delay of the desired size instead
   * @param reqIn The request signal that should be delayed
   * @param delay The delay value. Does not translate directly to any unit,
   *              but larger values = greater delay
   * @param conf The configuration object in use
   * @return
   */
  def synthDelay(reqIn: Bool, delay: Int)(implicit conf: ClickConfig): Bool = {
    if(delay > 0) {
      if(conf.SIMULATION) {
        val d = Module(new DelayElementSim(delay))
        d.io.reqIn := reqIn
        d.io.reqOut
      } else {
        val d = Module(new DelayElementSynth(delay))
        d.io.reqIn := reqIn
        d.io.reqOut
      }
    } else {
      reqIn
    }
  }

  /**
   * A bundle representing a bundled-data request/acknowledge handshake
   * @param gen
   * @tparam T
   */
  class ReqAck[T <: Data](gen: T) extends Bundle {
    val req = Input(Bool())
    val ack = Output(Bool())
    val data = Input(gen)
  }

  /**
   * A bundle holding two data items, making it easier to implement fork/join and logic blocks taking multiple inputs
   * @param gen1
   * @param gen2
   * @tparam T1
   * @tparam T2
   */
  class Bundle2[T1 <: Data, T2 <: Data](gen1: T1, gen2: T2) extends Bundle {
    val a = Output(gen1) //Using Output() to avoid clone type issues, fields are actually inputs
    val b = Output(gen2)
  }

  /**
   * An I/O bundle for a handshake port on a module
   * @param typ The datatype on the input and output of this block
   * @tparam T
   */
  class HandshakeIO[T <: Data](typ: T) extends Bundle {
    /** Handshake on input */
    val in = new ReqAck(typ)
    /** Input reset signal */
    val reset = Input(AsyncReset())
    /** Handshake on output */
    val out = Flipped(new ReqAck(typ))
  }
}
