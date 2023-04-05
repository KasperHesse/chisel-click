package examples

import chisel3._
import click._

/**
 * An N-stage FIFO where each register may have custom initial values and starting phases.
 * To generate a FIFO where all stages have the same initial value and same initial out.req-signal, use
 * the companion object
 * @param N The number of stages in the FIFO
 * @param init The initial value of each stage in the FIFO. init.length must equal N
 * @param ro   The initial value of the out.req signal of each FIFO stage. ro.length must equal N
 */
class Fifo[T <: Data](N: Int, init: Seq[T], ro: Seq[Boolean])(implicit conf: ClickConfig) extends RawModule {
  require(N > 0, "Number of stages must be positive")
  require(N == init.length, "Seq of initial values must equal length of FIFO")
  require(N == ro.length, "Seq of initial out.req-values must equal length of FIFO")

  val io = IO(new HandshakeIO(chiselTypeOf(init(0))))

  val stages = for(i <- 0 until N) yield {
    Module(new HandshakeRegister(init(i), ro(i)))
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

class MyFifo[T <: Data](N: Int, init: T, ro: Boolean)(implicit conf: ClickConfig) extends RawModule {
  val io = IO(new HandshakeIO(chiselTypeOf(init)))
  val stages = for(i <- 0 until N) yield {
    Module(new HandshakeRegister(init, ro))
  }
  for(i <- 0 until stages.length - 1) {
    stages(i).io.out <> stages(i+1).io.in
    stages(i).io.reset := io.reset
  }
  stages(N-1).io.reset := io.reset
  io.in <> stages(0).io.in
  io.out <> stages(N-1).io.out
}

object Fifo {
  /**
   * Creates an N-stage FIFO where all registers are initialized to the same values
   * @param N The number of stages in the FIFO
   * @param init The initial value of each data register in the FIFO
   * @param ro The initial value of each click element's out.req signal
   */
  def apply[T <: Data](N: Int, init: T, ro: Boolean)(implicit conf: ClickConfig) : Fifo[T]= {
    val inits = Seq.fill(N)(init)
    val ris = Seq.fill(N)(ro)
    new Fifo(N, inits, ris)
  }
}
