package examples

import chisel3._
import click._

/**
 * An N-stage fifo where each register may have custom initial values and starting phases.
 * To generate a FIFO where all stages have the same initial value and same initial out.req-signal, use
 * the companion object
 * @param N The number of stages in the fifo
 * @param init The initial value of each stage in the fifo. init.length must equal N
 * @param ri   The initial value of the out.req signal of each fifo stage. ri.length must equal N
 */
class Fifo[T <: Data](N: Int, init: Seq[T], ri: Seq[Boolean])(implicit conf: ClickConfig) extends RawModule {
  require(N > 0, "Number of stages must be positive")
  require(N == init.length, "Seq of initial values must equal length of FIFO")
  require(N == ri.length, "Seq of initial out.req-values must equal length of FIFO")

  val io = IO(new HandshakeIO(chiselTypeOf(init(0))))

  val stages = for(i <- 0 until N) yield {
    Module(new HandshakeRegister(init(i), ri(i)))
  }

  for(i <- 0 until stages.length - 1) {
    stages(i).io.out <> stages(i+1).io.in
    stages(i+1).io.in.req := simDelay(stages(i).io.out.req, conf.REG_DELAY)
    stages(i).io.reset := io.reset
  }
  stages(N-1).io.reset := io.reset
  io.in <> stages(0).io.in
  stages(0).io.in.req := simDelay(io.in.req, conf.REG_DELAY)
  io.out <> stages(N-1).io.out
}

object Fifo {
  /**
   * Creates an N-stage FIFO where all registers are initialized to the same values
   * @param N The number of stages in the FIFO
   * @param init The initial value of each data register in the FIFO
   * @param ri The initial value of each click element's out.req signal
   */
  def apply[T <: Data](N: Int, init: T, ri: Boolean)(implicit conf: ClickConfig) : Fifo[T]= {
    val inits = Seq.fill(N)(init)
    val ris = Seq.fill(N)(ri)
    new Fifo(N, inits, ris)
  }
}
