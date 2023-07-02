package click

import chisel3._
import chisel3.stage.ChiselStage
import examples.Fifo

/**
 * A 2-phase request-grant-done Mutex.
 * Since 2-phase signalling does not have return-to-zero signalling, a "done" signal is required
 * for mutexes. The done signal is in practice implemented through the acknowledge from the consumer
 * @param conf
 */
class RGDMutex(implicit conf: ClickConfig) extends Module with RequireAsyncReset {
  val io = IO(new Bundle {
    val R1 = Input(Bool())
    val D1 = Input(Bool())
    val G1 = Output(Bool())
    val R2 = Input(Bool())
    val D2 = Input(Bool())
    val G2 = Output(Bool())
  })
  //Implementation based on fig. 9.19
  //Regs[0..2] are the top row of registers from left to right. Regs[3..5] is bottom row, left to right
  val regs = for(_ <- 0 until 6) yield {
    Module(new PhaseRegister(false))
  }


  //Setup mutex inputs
  val mutex = Module(new BistableMutex())
  mutex.io.R1 := regs(0).io.out ^ io.D1
  mutex.io.R2 := regs(3).io.out ^ io.D2

  //Register clocking
  val clock1 = ((regs(0).io.out & !io.R1 & regs(1).io.out) | (!regs(0).io.out & io.R1 & !regs(1).io.out)).asClock
  val clock2 = ((regs(3).io.out & !io.R2 & regs(4).io.out) | (!regs(3).io.out & io.R2 & !regs(4).io.out)).asClock
  regs(0).clock := clock1
  regs(1).clock := (!mutex.io.G1).asClock
  regs(2).clock := mutex.io.G1.asClock
  regs(3).clock := clock2
  regs(4).clock := (!mutex.io.G2).asClock
  regs(5).clock := mutex.io.G2.asClock

  //Register inputs
  for(reg <- regs) {
    reg.io.in := !reg.io.out
//    reg.io.reset := this.reset.asAsyncReset
  }

  //Outputs
  io.G1 := regs(2).io.out
  io.G2 := regs(5).io.out
}

/**
 * A Mutex using two bistable NAND gates for ensuring mutual exclusion between two producers and one consumer.
 * When creating Verilog code from circuits using a BiStableMutex, the argument `--no-check-comb-loops` must be passed
 *
 * @param dR1 Delay from request R1 goes high until grant G1 may toggle. Defaults to 0 (no delay)
 * @param dR2 Delay from request R2 goes high until grant G2 may toggle. Defaults to 0 (no delay)
 */
class BistableMutex(val dR1: Int = 0, val dR2: Int = 0)
                   (implicit conf: ClickConfig) extends Module with RequireAsyncReset {
  val io = IO(new Bundle {
    val R1 = Input(Bool())
    val R2 = Input(Bool())
    val G1 = Output(Bool())
    val G2 = Output(Bool())
  })
  val o1 = Wire(Bool())
  val o2 = Wire(Bool())

  o1 := !(simDelay(io.R1, dR1) & o2)
  o2 := !(simDelay(io.R2, dR2) & o1)

  io.G1 := !o1 & o2
  io.G2 := o1 & !o2
}

object BistableMutex extends App {
  (new ChiselStage).emitVerilog(new BistableMutex()(ClickConfig()), Array("-td", "gen", "--no-check-comb-loops"))
}